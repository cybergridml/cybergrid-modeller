import {
  useSelection,
  WorkbenchViewComponentProps,
  WorkbenchViewHandle,
} from "@eclipse-sirius/sirius-components-core";
import { makeStyles } from "tss-react/mui";
import React, { ForwardedRef, forwardRef, useEffect, useState } from "react";
import { Alert, Button, Paper, Typography } from "@mui/material";
import { NotarizationViewState } from "./NotarizationView.types";
import { httpOrigin } from "../core/URL";
import {
  fetch_run_id,
  initialize_new_flow,
  notarize_process_model,
} from "./NotarizationApi";

const useStyles = makeStyles()((theme) => ({
  container: {
    height: "100%",
    width: "100%",
    display: "flex",
    flexDirection: "column",
    gap: theme.spacing(2),
    padding: theme.spacing(2),
  },
  section: {
    padding: theme.spacing(2),
    borderRadius: theme.shape.borderRadius,
    backgroundColor: theme.palette.background.paper,
    boxShadow: theme.shadows[1],
  },
  controlsContainer: {
    display: "flex",
    flexWrap: "wrap",
    gap: theme.spacing(2),
    marginTop: theme.spacing(2),
  },
  formGroup: {
    display: "flex",
    flexDirection: "column",
    gap: theme.spacing(2),
    marginTop: theme.spacing(2),
  },
}));

export const NotarizationView = forwardRef<
  WorkbenchViewHandle,
  WorkbenchViewComponentProps
>(
  (
    { editingContextId }: WorkbenchViewComponentProps,
    ref: ForwardedRef<WorkbenchViewHandle>
  ) => {
    const { classes } = useStyles();
    const [state, setState] = useState<NotarizationViewState>({
      currentSelection: { entries: [] },
      notarizationStatus: "NOT STARTED",
    });
    const [runId, setRunId] = React.useState(-1);
    const [responseText, setResponseText] = useState("");
    const { selection } = useSelection();

    const currentSelectionKey: string = state.currentSelection.entries
      .map((entry) => entry.id)
      .sort((a, b) => a.localeCompare(b))
      .join(":");

    const newSelectionKey: string = selection.entries
      .map((entry) => entry.id)
      .sort((a, b) => a.localeCompare(b))
      .join(":");

    useEffect(() => {
      if (
        selection.entries.length > 0 &&
        currentSelectionKey !== newSelectionKey
      ) {
        setState((prevState) => ({
          ...prevState,
          currentSelection: selection,
          notarizationStatus: "not started",
        }));
        setResponseText("");
      } else if (selection.entries.length === 0) {
        setState((prevState) => ({
          ...prevState,
          currentSelection: { entries: [] },
          notarizationStatus: "not started",
        }));
        setResponseText("");
      }
    }, [currentSelectionKey, newSelectionKey]);

    useEffect(() => {
      fetch_run_id().then((res) => {
        setRunId(res["runCount"] - 1);
      });
    });

    const initializeLedgeraFlow = async (
      event: React.FormEvent<HTMLFormElement>
    ) => {
      event.preventDefault();
      setResponseText("Initializing notarization process");
      setState((prev) => ({ ...prev, notarizationStatus: "pending" }));

      try {
        await initialize_new_flow().then(() => {
          setResponseText("");
          setState((prev) => ({ ...prev, notarizationStatus: "Successful" }));
        });
      } catch (error) {
        setResponseText(
          "Error during notarization: " + (error as Error).message
        );
        setState((prev) => ({
          ...prev,
          notarizationStatus: "Failed: " + (error as Error).message,
        }));
      }
    };

    const notarizeModel = async (event: React.FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      setResponseText("Model notarization started");
      setState((prev) => ({ ...prev, notarizationStatus: "pending" }));

      if (state.currentSelection.entries.length === 0) {
        setResponseText(
          "Error during notarization: select a model to notarize"
        );
        setState((prev) => ({ ...prev, notarizationStatus: "Failed" }));
        return;
      }

      const selectedEntry = state.currentSelection.entries[0];
      const documentId = selectedEntry.id ?? "undefined_name";

      try {
        const urlDoc = `${httpOrigin}/api/editingcontexts/${editingContextId}/documents/${documentId}`;
        await fetch(urlDoc, { headers: { Accept: "application/json" } })
          .then((res) => res.json())
          .then((resJson) => notarize_process_model(runId, resJson))
          .then(() => {
            setResponseText("");
            setState((prev) => ({ ...prev, notarizationStatus: "Successful" }));
          });
      } catch (error) {
        setResponseText(
          "Error during notarization: " + (error as Error).message
        );
        setState((prev) => ({ ...prev, notarizationStatus: "Failed" }));
      }
    };

    return (
      <div className={classes.container}>
        <Paper className={classes.section}>
          <Typography variant="h6">LCA Notarization</Typography>

          <div className={classes.controlsContainer}>
            <form id="notarize-form-id" onSubmit={initializeLedgeraFlow}>
              <Button variant="contained" type="submit">
                Initialize a new notarization flow
              </Button>
            </form>
          </div>

          <div className={classes.controlsContainer}>
            <form id="notarize-form-id" onSubmit={notarizeModel}>
              <Button variant="contained" type="submit">
                Notarize the model
              </Button>
            </form>
          </div>

          <Alert severity="info" sx={{ mt: 2 }}>
            <p>
              <strong>Notarization Status :</strong> {state.notarizationStatus}
            </p>
            <pre style={{ whiteSpace: "pre-wrap" }}>{responseText}</pre>
          </Alert>
        </Paper>
      </div>
    );
  }
);
