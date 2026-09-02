/**
 * Firebase Cloud Function: Stream Chat Token Generator
 * =====================================================
 * 
 * PURPOSE:
 *   Securely generates Stream Chat user tokens on the server side.
 *   This replaces the insecure devToken() call in the Android client.
 *
 * SETUP INSTRUCTIONS:
 *   1. Install Firebase CLI:        npm install -g firebase-tools
 *   2. Login to Firebase:           firebase login
 *   3. Initialize functions:        firebase init functions (choose JavaScript)
 *   4. Install dependency:          cd functions && npm install stream-chat
 *   5. Set the Stream secret:       firebase functions:config:set stream.secret="YOUR_STREAM_API_SECRET"
 *   6. Set the Stream API key:      firebase functions:config:set stream.key="79mj8qb8m3d7"
 *   7. Deploy:                      firebase deploy --only functions
 *
 * ANDROID CLIENT USAGE:
 *   After deploying, update ChatManager.getToken() to call this function:
 *
 *     private suspend fun getToken(userId: String): String {
 *         val idToken = Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
 *             ?: throw IllegalStateException("User not authenticated")
 *         val url = "https://<your-project>.cloudfunctions.net/getStreamToken"
 *         val request = Request.Builder()
 *             .url(url)
 *             .addHeader("Authorization", "Bearer $idToken")
 *             .build()
 *         val response = OkHttpClient().newCall(request).execute()
 *         val json = JSONObject(response.body?.string() ?: "")
 *         return json.getString("token")
 *     }
 *
 * STREAM DASHBOARD:
 *   After deploying this function, disable "Dev Mode" in the Stream Chat
 *   dashboard (https://dashboard.getstream.io/) to enforce token validation.
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { StreamChat } = require("stream-chat");

admin.initializeApp();

exports.getStreamToken = functions.https.onRequest(async (req, res) => {
  // Only allow POST requests
  if (req.method !== "POST" && req.method !== "GET") {
    res.status(405).send("Method Not Allowed");
    return;
  }

  // Extract and verify the Firebase Auth token from the Authorization header
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    res.status(401).json({ error: "Missing or invalid Authorization header" });
    return;
  }

  const firebaseIdToken = authHeader.split("Bearer ")[1];

  try {
    // Verify the Firebase ID token — this ensures the caller is authenticated
    const decodedToken = await admin.auth().verifyIdToken(firebaseIdToken);
    const userId = decodedToken.uid;

    // Initialize the Stream Chat server client using the secret (server-side only)
    const streamApiKey = functions.config().stream.key;
    const streamApiSecret = functions.config().stream.secret;
    const serverClient = StreamChat.getInstance(streamApiKey, streamApiSecret);

    // Generate a signed token for this specific user
    const streamToken = serverClient.createToken(userId);

    res.status(200).json({ token: streamToken });
  } catch (error) {
    console.error("Token generation failed:", error);
    res.status(401).json({ error: "Authentication failed" });
  }
});
