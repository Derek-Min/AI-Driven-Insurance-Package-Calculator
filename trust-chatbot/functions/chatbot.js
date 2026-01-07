export async function onRequestPost({ request }) {
    const body = await request.text();

    const response = await fetch("http://3.238.58.121:8080/chatbot", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body
    });

    return new Response(await response.text(), {
        headers: { "Content-Type": "application/json" }
    });
}
