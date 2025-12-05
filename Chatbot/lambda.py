# lambda_function.py
import json
import os
import re
import time
import uuid
import logging
import urllib.request
from urllib.error import HTTPError, URLError
import boto3
from decimal import Decimal

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Environment variables to set in Lambda configuration
BACKEND_URL = os.environ.get("BACKEND_URL", "http://your-backend-host:8080")
DDB_TABLE = os.environ.get("DDB_TABLE", "ChatSessions")
DEFAULT_FROM = os.environ.get("DEFAULT_FROM", "no-reply@example.com")

dynamodb = boto3.resource("dynamodb")
table = dynamodb.Table(DDB_TABLE)


# -----------------------
# Utilities
# -----------------------
def http_post_json(url, data, timeout=10):
    """Simple POST JSON helper using urllib (no external deps)."""
    payload = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except HTTPError as e:
        logger.error("HTTPError %s: %s", e.code, e.read().decode("utf-8"))
        raise
    except URLError as e:
        logger.error("URLError: %s", e)
        raise


def is_affirmative(text):
    text = (text or "").strip().lower()
    return text in ("yes", "y", "sure", "ok", "please", "send", "yep", "yeah")


def find_numbers(text):
    return re.findall(r"\d+", text or "")


# -----------------------
# Intent detection
# -----------------------
MOTOR_KEYWORDS = ["motor", "car", "vehicle", "auto", "comprehensive", "roadtax", "ncd", "plate", "insurance"]
LIFE_KEYWORDS = ["life", "protection", "medical", "death", "income", "beneficiary", "shortterm", "short-term"]

def detect_intent(message):
    msg = (message or "").lower()
    if any(k in msg for k in MOTOR_KEYWORDS):
        return "MOTOR"
    if any(k in msg for k in LIFE_KEYWORDS):
        return "LIFE"
    return None


# -----------------------
# Question flows & slots
# -----------------------
MOTOR_SLOTS = [
    ("customer_name", "What's your full name?"),
    ("email", "Please provide your email address."),
    ("make", "Vehicle make (e.g. Perodua)"),
    ("model", "Vehicle model (e.g. Myvi)"),
    ("year", "Year of manufacture (e.g. 2019)"),
    ("plate_no", "Plate number"),
    ("usage", "Usage: Private or Commercial Truck?"),
    ("region", "Which region is your vehicle registered in?"),
    ("ncd_percent", "What is your NCD percentage? (e.g. 38)"),
    ("sum_insured", "What sum insured do you want? (numeric, e.g. 40000)"),
    ("optional_coverages", "Any optional coverages? (type names like Theft, ACT_OF_GOD, SRCC or 'none')"),
]

LIFE_SLOTS = [
    ("customer_name", "What's your full name?"),
    ("email", "Please provide your email address."),
    ("age", "What's your age?"),
    ("gender", "Gender (Male / Female / Other)"),
    ("income", "Monthly income (RM)"),
    ("smoker_status", "Do you smoke? (Yes / No)"),
    ("occupation", "Occupation"),
    ("health_flags", "Any pre-existing health conditions? (none if none)"),
]

# maps a session's intent to required slots
SLOT_MAP = {
    "MOTOR": MOTOR_SLOTS,
    "LIFE": LIFE_SLOTS
}


# -----------------------
# DynamoDB session helpers
# -----------------------
def get_session(session_id):
    """Fetch session or return None."""
    try:
        resp = table.get_item(Key={"sessionId": session_id})
        return resp.get("Item")
    except Exception as e:
        logger.error("DDB get error: %s", e)
        return None


def save_session(session_id, state):
    """Save session state (state must be JSON-serializable)."""
    # convert floats/decimals appropriately; here keep it simple
    state["updatedAt"] = int(time.time())
    table.put_item(Item={"sessionId": session_id, **state})


def clear_session(session_id):
    try:
        table.delete_item(Key={"sessionId": session_id})
    except Exception as e:
        logger.warning("Failed to delete session %s: %s", session_id, e)


# -----------------------
# Build API payloads
# -----------------------
def build_attributes_from_state(intent, slots):
    """Turn collected slots into attributes map expected by backend."""
    attrs = {}
    for k, _q in SLOT_MAP.get(intent, []):
        v = slots.get(k)
        if v is None:
            continue
        # transform certain slots:
        if k in ("age", "year", "sum_insured", "income", "ncd_percent"):
            try:
                # numeric conversions
                if "." in str(v):
                    attrs[k] = float(v)
                else:
                    attrs[k] = int(v)
            except Exception:
                # fallback to raw string
                attrs[k] = v
        else:
            attrs[k] = v
    # for optional coverages: convert human text to flags
    if intent == "MOTOR":
        cov_text = slots.get("optional_coverages", "")
        # simple parsing: split by comma/space and uppercase tokens
        if cov_text and cov_text.strip().lower() != "none":
            tokens = re.split(r"[,\s;]+", cov_text.strip())
            for t in tokens:
                if not t:
                    continue
                code = normalize_coverage_code(t)
                attrs[f"{code}_enabled"] = True
    return attrs


def normalize_coverage_code(token):
    token = token.strip().upper()
    token = token.replace("-", "_").replace(" ", "_")
    # map common words
    mapping = {
        "THEFT": "THEFT",
        "ACT": "ACT_OF_GOD",
        "ACT_OF_GOD": "ACT_OF_GOD",
        "AOG": "ACT_OF_GOD",
        "SRCC": "SRCC",
        "NIL": "NIL_EXCESS",
        "NIL_EXCESS": "NIL_EXCESS",
        "FRONT_GLASS": "FRONT_GLASS",
        "GLASS": "FRONT_GLASS",
    }
    return mapping.get(token, token)


# -----------------------
# Main handler
# -----------------------
def lambda_handler(event, context):
    """
    Expected event:
    {
      "sessionId": "string",       # unique per user / browser
      "inputText": "Hello",
      "source": "web"              # optional
    }
    Response (simple):
    {
      "sessionId": "xxx",
      "messages": [{"type":"text","text":"..."}],
      "shouldEndSession": false
    }
    """
    try:
        body = event if isinstance(event, dict) else json.loads(event["body"])
    except Exception:
        body = event

    session_id = body.get("sessionId") or str(uuid.uuid4())
    user_text = (body.get("inputText") or "").strip()

    # load session state
    session = get_session(session_id) or {}
    intent = session.get("intent")
    slots = session.get("slots", {})  # collected slots
    step = session.get("step", 0)

    # If no intent yet, try to detect it from the user_text
    if not intent:
        detected = detect_intent(user_text)
        if detected:
            intent = detected
            session["intent"] = intent
            session["slots"] = slots
            session["step"] = 0
            save_session(session_id, session)
            # send first question
            question = SLOT_MAP[intent][0][1]
            return make_response(session_id, [f"Great — let's start your {intent} quotation.", question])

        # ask user to choose
        return {
                "statusCode": 200,
                "headers": {
                    "Access-Control-Allow-Origin": "*",
                    "Access-Control-Allow-Headers": "*",
                    "Access-Control-Allow-Methods": "GET,POST,OPTIONS"
                },
                "body": json.dumps({
                    "messages": [
                        {"text": "Your chatbot message goes here"}
                    ]
                })

    # If intent exists, continue the slot collection flow
    required_slots = SLOT_MAP[intent]

    # If user answered "cancel" or "restart"
    if user_text.lower() in ("cancel", "restart", "start over"):
        clear_session(session_id)
       return make_response(session_id, ["What would you like to get a quotation for? Motor or Life?"])


    # If user replies "I want Motor" or "I want Life" mid-session, switch intent
    maybe_intent = detect_intent(user_text)
    if maybe_intent and maybe_intent != intent:
        session = {"intent": maybe_intent, "slots": {}, "step": 0}
        save_session(session_id, session)
        question = SLOT_MAP[maybe_intent][0][1]
        return make_response(session_id, [f"Switching to {maybe_intent} flow.", question])

    # If step indicates we are awaiting an answer for required_slots[step]
    if step < len(required_slots):
        slot_key, question_text = required_slots[step]
        # parse simple email/yes/no/year etc.
        parsed = parse_slot_answer(slot_key, user_text)
        if parsed is not None:
            slots[slot_key] = parsed
            session["slots"] = slots
            session["step"] = step + 1
            save_session(session_id, session)
        else:
            # if parsing failed, ask again
            return make_response(session_id, [f"Sorry, I couldn't understand that. {question_text}"])

    # After storing, if slots remain, ask next question
    step = session.get("step", 0)
    if step < len(required_slots):
        next_q = required_slots[step][1]
        return make_response(session_id, [next_q])

    # All required slots collected — call preview
    attributes = build_attributes_from_state(intent, slots)
    preview_payload = {"line": intent, "attributes": attributes}
    preview_url = f"{BACKEND_URL}/api/quotes/preview"

    try:
        preview_result = http_post_json(preview_url, preview_payload)
    except Exception as e:
        logger.exception("Preview API error")
        return make_response(session_id, ["Sorry, I couldn't calculate a quote right now. Please try again later."])

    # Build a friendly summary
    summary = build_summary_from_preview(preview_result, intent, slots)
    # Save preview result and ask to email
    session["previewResult"] = preview_result
    session["slots"] = slots
    save_session(session_id, session)

    messages = [summary, "Would you like me to email the full quotation to you? (Yes / No)"]
   return make_response(session_id, ["What would you like to get a quotation for? Motor or Life?"])



def parse_slot_answer(slot_key, text):
    """Simple parsing heuristics for common slot types."""
    if slot_key == "email":
        text = text.strip()
        if re.match(r"[^@]+@[^@]+\.[^@]+", text):
            return text
        return None
    if slot_key in ("year", "age"):
        nums = find_numbers(text)
        return int(nums[0]) if nums else None
    if slot_key in ("sum_insured", "income", "ncd_percent"):
        nums = re.findall(r"[\d\.]+", text)
        if not nums:
            return None
        # choose first numeric token
        val = nums[0]
        return float(val) if "." in val else int(val)
    if slot_key == "usage":
        if "commercial" in text.lower() or "truck" in text.lower():
            return "Commercial Truck"
        if "private" in text.lower() or "personal" in text.lower():
            return "Private"
        return text
    if slot_key == "optional_coverages":
        return text
    # default: store raw text
    return text.strip() if text else None


def build_summary_from_preview(preview_result, intent, slots):
    """Turns preview_result (PremiumResult) into a short human-readable message."""
    breakdown = preview_result.get("breakdown") or {}
    items = breakdown.get("items", [])
    currency = breakdown.get("currency", "MYR")
    sum_insured = breakdown.get("sumInsured") or breakdown.get("sum_insured") or slots.get("sum_insured")
    total = breakdown.get("totalPremium") or preview_result.get("totalPremium") or preview_result.get("total_premium")

    lines = []
    if intent == "MOTOR":
        vdesc = f"{slots.get('make','')} {slots.get('model','')} {slots.get('year','')}".strip()
        lines.append(f"Quotation for your {vdesc}:")
    else:
        lines.append("Life insurance quotation:")

    lines.append(f"- Sum Insured: {currency} {sum_insured}")
    lines.append(f"- Total Premium: {currency} {total}")
    if items:
        lines.append("- Breakdown:")
        for it in items[:10]:
            label = it.get("label") or it.get("code") or "Item"
            amount = it.get("amount") or it.get("amount")
            lines.append(f"  • {label}: {currency} {amount}")
    # short reason
    lines.append(f"- Risk Score: {preview_result.get('riskScore', preview_result.get('risk_score','N/A'))}")

    return "\n".join(lines)


def make_response(session_id, messages, end_session=False):
    return {
        "statusCode": 200,
        "headers": {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Headers": "*",
            "Access-Control-Allow-Methods": "GET, POST, OPTIONS"
        },
        "body": json.dumps({
            "sessionId": session_id,
            "messages": [
                {"type": "text", "text": m}
                for m in (messages if isinstance(messages, list) else [messages])
            ],
            "shouldEndSession": end_session
        })
    }



# -----------------------
# Optional helper to accept "confirm send" from the user
# Triggered by sending "yes" after preview
# This can be invoked by your chat frontend by posting {"action":"confirm_send","sessionId":"..."}
# -----------------------
def handle_confirm_send(event):
    body = event if isinstance(event, dict) else json.loads(event.get("body","{}"))
    session_id = body.get("sessionId")
    session = get_session(session_id)
    if not session:
        return make_response(session_id, ["Session expired. Please start a new quote."])

    preview = session.get("previewResult")
    slots = session.get("slots", {})
    if not preview:
        return make_response(session_id, ["No preview found. Please create a quote preview first."])

    # build create payload -> POST /api/quotes (this should save and trigger SES email on backend)
    create_payload = {"line": session.get("intent"), "attributes": build_attributes_from_state(session.get("intent"), slots)}
    create_url = f"{BACKEND_URL}/api/quotes"
    try:
        resp = http_post_json(create_url, create_payload)
    except Exception:
        return make_response(session_id, ["Sorry, we couldn't send the email at this time. Try again later."])

    # after sending, optionally clear session
    clear_session(session_id)
    return make_response(session_id, [f"Your quotation has been emailed. Reference: {resp.get('id','(no ref)')}"], end_session=True)


# -----------------------
# Lambda entry for confirm endpoint (if you want single function)
# -----------------------
def lambda_handler_confirm(event, context):
    # support a simple action: {"action":"confirm_send", "sessionId":"..."} or normal chat flow
    try:
        body = event if isinstance(event, dict) else json.loads(event.get("body","{}"))
    except Exception:
        body = event

    if body.get("action") == "confirm_send":
        return handle_confirm_send(body)
    # otherwise fallback to chat flow
    return lambda_handler(event, context)
