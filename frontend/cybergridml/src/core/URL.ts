const { VITE_HTTP_SERVER_PORT, VITE_WS_SERVER_PORT } = import.meta.env;

let httpURL = "";
if (!VITE_HTTP_SERVER_PORT || VITE_HTTP_SERVER_PORT.length === 0) {
  httpURL = `${window.location.protocol}//${window.location.host}`;
} else {
  httpURL = `${window.location.protocol}//${window.location.hostname}:${VITE_HTTP_SERVER_PORT}`;
}
export const httpOrigin = httpURL;

let wsURL = "";
if (!VITE_WS_SERVER_PORT || VITE_WS_SERVER_PORT.length === 0) {
  let wsProtocol = "ws:";
  if ("https:" === window.location.protocol) {
    wsProtocol = "wss:";
  }
  wsURL = `${wsProtocol}//${window.location.host}`;
} else {
  wsURL = `ws://${window.location.hostname}:${VITE_WS_SERVER_PORT}`;
}
export const wsOrigin = wsURL;
