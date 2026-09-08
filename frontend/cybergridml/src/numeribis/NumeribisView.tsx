import {
  WorkbenchViewComponentProps,
  WorkbenchViewHandle,
} from "@eclipse-sirius/sirius-components-core";
import { ForwardedRef, forwardRef } from "react";
import { NumeribisViewState } from "./NumeribisView.types";

export const NumeribisView = forwardRef<
  WorkbenchViewHandle,
  WorkbenchViewComponentProps
>(
  (
    _props: WorkbenchViewComponentProps,
    _ref: ForwardedRef<WorkbenchViewHandle>
  ) => {
    const state: NumeribisViewState = {
      url: "http://localhost:8501",
    };

    return (
      <div
        style={{
          height: "100%",
          width: "100%",
          overflow: "hidden",
        }}
      >
        <iframe
          id="iFrameNumeribis"
          title="Numeribis"
          src={state.url}
          style={{
            width: "100%",
            height: "100%",
            border: "none",
          }}
          allowFullScreen
        />
      </div>
    );
  }
);

NumeribisView.displayName = "NumeribisView";

export default NumeribisView;
