export async function onRequestPost({ request }) {
    const body = await request.text();

    const res = await fetch(
        "https://api.trust-insurancexyz.xyz/api/chatbot",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body
        }
    );

    return new Response(await res.text(), {
        status: res.status,
        headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*"
        }
    });
}
