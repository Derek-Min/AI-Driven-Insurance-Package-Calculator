export async function onRequest(context) {
    // Handle OPTIONS preflight requests
    if (context.request.method === 'OPTIONS') {
        return new Response(null, {
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type',
            },
        });
    }

    const url = new URL(context.request.url);
    const apiPath = context.params.path.join('/');

    // Your actual API endpoint
    const apiUrl = `http://api.trust-insurance.com/${apiPath}${url.search}`;

    try {
        // Forward the request
        const response = await fetch(apiUrl, {
            method: context.request.method,
            headers: {
                'Content-Type': 'application/json',
            },
            body: context.request.method !== 'GET' ? context.request.body : undefined,
        });

        // Get the response body
        const data = await response.text();

        // Return the response with CORS headers
        return new Response(data, {
            status: response.status,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type',
            },
        });
    } catch (error) {
        return new Response(JSON.stringify({
            messages: ["⚠️ Backend not reachable"],
            endSession: false
        }), {
            status: 500,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*',
            },
        });
    }
}