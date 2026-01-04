from flask import Flask, request, jsonify
import json
from lambda_function import lambda_handler

app = Flask(__name__)

@app.route("/chatbot", methods=["POST", "OPTIONS"])
def chatbot():
    if request.method == "OPTIONS":
        return ("", 200)

    body = request.get_json(silent=True) or {}
    event = {
        "body": json.dumps(body)
    }

    result = lambda_handler(event, None)

    status = result.get("statusCode", 200)
    response_body = result.get("body", "{}")

    try:
        data = json.loads(response_body)
    except Exception:
        data = {"error": response_body}

    return jsonify(data), status


if __name__ == "__main__":
    print("✅ Local Lambda running at http://localhost:3001/chatbot")
    app.run(host="0.0.0.0", port=3001, debug=True)
