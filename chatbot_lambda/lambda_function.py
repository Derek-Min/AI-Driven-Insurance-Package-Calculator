import json
import os
import time
import uuid
import logging
import urllib.request
import re
from pymongo import MongoClient

# -------------------------------------------------
# Configuration
# -------------------------------------------------
BACKEND_URL = os.environ.get("BACKEND_URL", "http://localhost:8080")
MONGO_URI = os.environ.get("MONGO_URI", "mongodb://localhost:27017")
DB_NAME = "insurance_chatbot"

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# -------------------------------------------------
# MongoDB
# -------------------------------------------------
mongo_client = MongoClient(MONGO_URI)
db = mongo_client[DB_NAME]
sessions = db.sessions

# -------------------------------------------------
# Regex Rules
# -------------------------------------------------
REGEX = {
    "customer_name": r"^[A-Za-z\s.'-]{3,100}$",
    "email": r"^[^@]+@[^@]+\.[^@]+$",
    "make": r"^[A-Za-z0-9\s.-]{2,50}$",
    "model": r"^[A-Za-z0-9\s.-]{1,50}$",
    "plate_no": r"^[A-Z0-9\-]{3,10}$",
    "region": r"^[A-Za-z\s]{2,30}$",
    "year": r"^(19[8-9]\d|20[0-2]\d)$",
    "ncd_percent": r"^(0|[1-9]\d?|100)$",
    "sum_insured": r"^[1-9]\d{3,8}$",
    "age": r"^(1[8-9]|[2-9]\d)$",
    "income": r"^[1-9]\d{2,8}$",
}

def regex_validate(key, value):
    return re.match(REGEX.get(key, ".*"), str(value), re.IGNORECASE) is not None

# -------------------------------------------------
# Helpers
# -------------------------------------------------
def http_post_json(url, data):
    payload = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=payload,
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req, timeout=20) as r:
        return json.loads(r.read().decode())

def detect_intent(msg):
    msg = (msg or "").lower()
    if any(k in msg for k in ["motor", "car", "vehicle"]):
        return "MOTOR"
    if any(k in msg for k in ["life", "medical", "income"]):
        return "LIFE"
    return None

def normalize_usage(text):
    t = (text or "").lower()
    if t in ["private", "personal"]:
        return "Private"
    if t in ["commercial", "business", "grab", "delivery"]:
        return "Commercial"
    return None

def normalize_gender(text):
    t = (text or "").lower()
    if t in ["male", "m"]:
        return "Male"
    if t in ["female", "f"]:
        return "Female"
    return None

def normalize_smoker(text):
    t = (text or "").lower()
    if t in ["yes", "y"]:
        return "Yes"
    if t in ["no", "n"]:
        return "No"
    return None

# -------------------------------------------------
# MongoDB Session Helpers
# -------------------------------------------------
def get_session(sid):
    return sessions.find_one({"sessionId": sid})

def save_session(sid, state):
    state["sessionId"] = sid
    state["updatedAt"] = int(time.time())
    sessions.update_one(
        {"sessionId": sid},
        {"$set": state},
        upsert=True
    )

def clear_session(sid):
    sessions.delete_one({"sessionId": sid})

def make_response(sid, messages, end=False):
    if not isinstance(messages, list):
        messages = [messages]
    return {
        "statusCode": 200,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Headers": "*",
            "Access-Control-Allow-Methods": "OPTIONS,POST"
        },
        "body": json.dumps({
            "sessionId": sid,
            "messages": messages,
            "endSession": end
        })
    }

# -------------------------------------------------
# Slot Definitions
# -------------------------------------------------
SLOT_MAP = {
    "MOTOR": [
        ("customer_name", "May I have your full name as per NRIC or passport?"),
        ("email", "Please provide your email address."),
        ("make", "What is the vehicle make?"),
        ("model", "What is the vehicle model?"),
        ("year", "What is the year of manufacture?"),
        ("plate_no", "What is the vehicle plate number?"),
        ("usage", "Is the usage Private or Commercial?"),
        ("region", "Which region is the vehicle registered in?"),
        ("ncd_percent", "What is your NCD percentage?"),
        ("sum_insured", "What is the sum insured (RM)?")
    ],
    "LIFE": [
        ("customer_name", "May I have your full name?"),
        ("email", "Please provide your email address."),
        ("age", "What is your age?"),
        ("gender", "What is your gender? (Male / Female)"),
        ("smoker_status", "Do you smoke? (Yes / No)"),
        ("income", "What is your monthly income (RM)?")
    ]
}

# -------------------------------------------------
# Lambda Handler
# -------------------------------------------------
def lambda_handler(event, context=None):

    # Handle preflight (important for local proxy)
    if event.get("httpMethod") == "OPTIONS":
        return make_response("na", [], end=False)

    # Safe body parsing
    try:
        body = json.loads(event.get("body") or "{}")
    except Exception:
        body = {}

    session_id = body.get("sessionId") or str(uuid.uuid4())
    user_text = str(body.get("message") or "").strip()

    if user_text.lower() in ["restart", "reset"]:
        clear_session(session_id)
        return make_response(session_id, "Welcome! Do you want Motor or Life insurance?")

    session = get_session(session_id)

    # -------------------------
    # New session
    # -------------------------
    if not session:
        intent = detect_intent(user_text)
        if not intent:
            save_session(session_id, {"intent": None, "slots": {}})
            return make_response(session_id, "Welcome! Do you want Motor or Life insurance?")
        save_session(session_id, {"intent": intent, "slots": {}})
        return make_response(session_id, SLOT_MAP[intent][0][1])

    intent = session.get("intent")
    slots = session.get("slots", {})

    # -------------------------
    # Ask intent if missing
    # -------------------------
    if not intent:
        intent = detect_intent(user_text)
        if not intent:
            return make_response(session_id, "Please choose Motor or Life insurance.")
        session["intent"] = intent
        save_session(session_id, session)
        return make_response(session_id, SLOT_MAP[intent][0][1])

    # -------------------------
    # Slot filling
    # -------------------------
    required = SLOT_MAP[intent]
    step = len([k for k, _ in required if slots.get(k)])

    if step < len(required):
        key, _ = required[step]
        value = user_text

        if key == "usage":
            value = normalize_usage(value)
            if not value:
                return make_response(session_id, "Please reply Private or Commercial.")

        if key == "gender":
            value = normalize_gender(value)
            if not value:
                return make_response(session_id, "Please reply Male or Female.")

        if key == "smoker_status":
            value = normalize_smoker(value)
            if not value:
                return make_response(session_id, "Please reply Yes or No.")

        if not regex_validate(key, value):
            return make_response(session_id, f"Invalid {key.replace('_',' ')} format.")

        slots[key] = value
        session["slots"] = slots
        save_session(session_id, session)

        if step + 1 < len(required):
            return make_response(session_id, required[step + 1][1])

    # -------------------------
    # Call Spring Boot backend
    # -------------------------
    try:
        resp = http_post_json(
            f"{BACKEND_URL}/api/quote/{intent.lower()}/from-chat",
            {"sessionId": session_id, "intent": intent, "slots": slots}
        )

        if not resp.get("ok"):
            raise Exception(resp.get("error", "Backend failed"))

    except Exception as e:
        logger.error("Backend error: %s", e)
        return make_response(
            session_id,
            [
                "✅ Your details are complete.",
                "⚠️ Unable to generate quotation now. Please try again later."
            ],
            end=True
        )

    clear_session(session_id)

    return make_response(
        session_id,
        [
            "✅ Your quotation has been generated successfully!",
            f"📄 {intent.capitalize()} Insurance quotation sent to your email.",
            "Thank you for choosing Trust Insurance 🙏"
        ],
        end=True
    )
