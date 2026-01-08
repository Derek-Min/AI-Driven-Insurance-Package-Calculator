export async function onRequestPost({ request }) {
    const body = await request.text();

    const res = await fetch("http://127.0.0.1:8080/chatbot", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body
    });

    return new Response(await res.text(), {
        headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*"
        }
    });
}
