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

/**
 * File picker shown when the "Import XIIDM file" tool is invoked on a grid node.
 *
 * The backend tool declares a dialog so that Sirius asks the frontend for tool variables before running it. This
 * component replaces the built-in selection dialog for that tool, reads the chosen file, and hands its content
 * back as the `xiidmContent` and `xiidmFileName` tool variables.
 */
export const XiidmImportDialog = ({
  onClose,
  onFinish,
}: DiagramDialogComponentProps) => {
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [reading, setReading] = useState<boolean>(false);

  const onImport = async () => {
    if (!file) {
      return;
    }
    setReading(true);
    setError(null);
    try {
      const xiidmContent = await file.text();
      onFinish([
        { name: "xiidmContent", value: xiidmContent, type: "STRING" },
        { name: "xiidmFileName", value: file.name, type: "STRING" },
      ]);
    } catch (exception) {
      setReading(false);
      setError(`The file could not be read: ${exception}`);
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
          disabled={!file || reading}
          onClick={onImport}
          data-testid="import-xiidm-submit"
        >
          Import
        </Button>
      </DialogActions>
    </Dialog>
  );
};
