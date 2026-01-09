export async function onRequest(context) {
    if (context.request.method === "OPTIONS") {
        return new Response(null, {
            headers: {
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "POST,OPTIONS",
                "Access-Control-Allow-Headers": "Content-Type"
            }
        });
    }

    const url = new URL(context.request.url);
    const apiPath = context.params.path.join("/");

    const apiUrl = `https://api.trust-insurancexyz.xyz/api/${apiPath}`;

    const response = await fetch(apiUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: await context.request.text()
    });

    return new Response(await response.text(), {
        status: response.status,
        headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*"
        }
    });
}
