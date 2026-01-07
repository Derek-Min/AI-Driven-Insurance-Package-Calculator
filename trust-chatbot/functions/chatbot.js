export async function onRequestPost(context) {
    const body = await context.request.text();

    const response = await fetch("http://3.238.58.121:8080/chatbot", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body
    });

    return new Response(await response.text(), {
        status: response.status,
        headers: {
            "Content-Type": "application/json"
        }
    });
}
