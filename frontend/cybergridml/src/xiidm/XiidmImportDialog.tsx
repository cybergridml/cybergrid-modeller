import { DiagramDialogComponentProps } from "@eclipse-sirius/sirius-components-diagrams";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";
import Typography from "@mui/material/Typography";
import { useState } from "react";

export const XiidmImportDialog = ({
  editingContextId,
  variables,
  onClose,
  onFinish,
}: DiagramDialogComponentProps) => {
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [uploading, setUploading] = useState<boolean>(false);

  const targetElementId =
    variables.find((v) => v.name === "targetObjectId")?.value ?? "";

  const onImport = async () => {
    if (!file) {
      return;
    }
    if (!targetElementId) {
      setError("Target element ID is not available");
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("editingContextId", editingContextId);
      formData.append("targetElementId", targetElementId);

      const response = await fetch("/api/xiidm/import", {
        method: "POST",
        body: formData,
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.error || `Upload failed: ${response.status}`);
      }

      // Trigger tool body to expose substations in diagram
      onFinish([
        { name: "xiidmContent", value: "", type: "STRING" },
        { name: "xiidmFileName", value: file.name, type: "STRING" },
      ]);
    } catch (exception) {
      setUploading(false);
      setError(
        `Import failed: ${
          exception instanceof Error ? exception.message : exception
        }`
      );
    }
  };

  return (
    <Dialog
      open
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      data-testid="import-xiidm-dialog"
    >
      <DialogTitle>Import an XIIDM file</DialogTitle>
      <DialogContent>
        <DialogContentText>
          The substations, buses, generators, loads and lines described by the
          file will be created inside this grid. Lines and transformers become a
          pair of terminals, one under each of the buses they connect.
        </DialogContentText>
        <Button
          variant="outlined"
          component="label"
          sx={{ mt: 2 }}
          data-testid="import-xiidm-select-file"
        >
          Select a file
          <input
            type="file"
            accept=".xiidm,.xml"
            hidden
            onChange={(event) => {
              setFile(event.target.files?.[0] ?? null);
              setError(null);
            }}
          />
        </Button>
        {file ? (
          <Typography
            variant="body2"
            sx={{ mt: 1 }}
            data-testid="import-xiidm-file-name"
          >
            {file.name}
          </Typography>
        ) : null}
        {error ? (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        ) : null}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          disabled={!file || uploading}
          onClick={onImport}
          data-testid="import-xiidm-submit"
        >
          {uploading ? "Importing..." : "Import"}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
