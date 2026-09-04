import { createTheme } from "@mui/material/styles";

// Mirrors @eclipse-sirius/sirius-web-application's siriusWebTheme, with the
// left/right sidebar backgrounds made solid so the view-selector icons stand
// out (the default theme uses the brand colors at 50% opacity, which leaves
// the disabled/unselected icons hard to see).
const container = () => document.fullscreenElement ?? document.body;

const baseTheme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#BE1A78",
      dark: "#851254",
      light: "#CB4793",
    },
    secondary: {
      main: "#261E58",
      dark: "#1A153D",
      light: "#514B79",
    },
    text: {
      primary: "#261E58",
      disabled: "#B3BFC5",
    },
    error: {
      main: "#DE1000",
      dark: "#9B0B00",
      light: "#E43F33",
    },
    success: {
      main: "#43A047",
      dark: "#327836",
      light: "#4EBA54",
    },
    warning: {
      main: "#FF9800",
      dark: "#D98200",
      light: "#FFB800",
    },
    info: {
      main: "#2196F3",
      dark: "#1D7DCC",
      light: "#24A7FF",
    },
    divider: "#B3BFC5",
    navigation: {
      leftBackground: "#851254",
      rightBackground: "#1A153D",
    },
    navigationBar: {
      border: "#BE1A78",
      background: "#261E58",
    },
    selected: "#BE1A78",
    action: {
      hover: "#BE1A7826",
      selected: "#BE1A7842",
      disabledBackground: "#E0E0E0",
    },
  },
});

export const cybergridMLTheme = createTheme(
  {
    components: {
      MuiAvatar: {
        styleOverrides: {
          colorDefault: { backgroundColor: baseTheme.palette.primary.main },
        },
      },
      MuiMenu: { defaultProps: { container } },
      MuiTooltip: {
        defaultProps: { PopperProps: { container } },
        styleOverrides: {
          tooltip: { backgroundColor: baseTheme.palette.common.black },
        },
      },
      MuiLink: { defaultProps: { underline: "hover" } },
    },
  },
  baseTheme
);
