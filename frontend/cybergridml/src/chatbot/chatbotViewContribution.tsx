import { WorkbenchViewContribution } from "@eclipse-sirius/sirius-components-core";
import ChatIcon from "@mui/icons-material/Chat";
import { ChatbotView } from "./ChatbotView";

export const chatbotViewContribution: WorkbenchViewContribution = {
  id: "cybergridml-chatbot",
  title: "Chatbot",
  icon: <ChatIcon />,
  component: ChatbotView,
};
