import {
  WorkbenchViewComponentProps,
  WorkbenchViewHandle,
} from "@eclipse-sirius/sirius-components-core";
import Typography from "@mui/material/Typography";
import { ForwardedRef, forwardRef, useImperativeHandle } from "react";

export const ChatbotView = forwardRef<
  WorkbenchViewHandle,
  WorkbenchViewComponentProps
>(({ id }: WorkbenchViewComponentProps, ref: ForwardedRef<WorkbenchViewHandle>) => {
  useImperativeHandle(ref, () => ({
    id,
    getWorkbenchViewConfiguration: () => ({}),
    applySelection: null,
  }));

  return (
    <div style={{ padding: 16 }} data-testid="view-Chatbot">
      <Typography variant="h6">Chatbot</Typography>
    </div>
  );
});

ChatbotView.displayName = "ChatbotView";
