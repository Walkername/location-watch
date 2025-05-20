
export default function getClaimFromToken(token, claim) {
    try {
      // Split the token into its three parts: header, payload, and signature
      const base64Url = token.split('.')[1]; // Get the payload part
      if (!base64Url) return null;
  
      // Decode the Base64 URL-encoded payload
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const decodedPayload = JSON.parse(window.atob(base64));
  
      // Return the specific claim if it exists
      return decodedPayload[claim] || null;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }