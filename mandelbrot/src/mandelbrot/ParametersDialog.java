package mandelbrot;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ParametersDialog extends MessageDialog {
	private Integer iterations;
	private Text text;
	private Parameters parameters;
	
	public ParametersDialog(Shell parentShell, Parameters parameters) {
		super(parentShell, Constants.PARAMETERS_TITLE, null, parameters.toString(), MessageDialog.INFORMATION, Constants.INFO_BUTTONS, 0);
		this.parameters = parameters;
	}

	protected Point getInitialSize() {
		return new Point(400, 300);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);
		container.setLayout(new GridLayout(3, false));
		new Label(container, SWT.NONE).setText("Iterations: ");
		text = new Text(container, SWT.BORDER);
		text.setText(String.valueOf(parameters.getIterations()));
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return container;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			iterations = Integer.valueOf(text.getText());
		}
		super.buttonPressed(buttonId);
	}
}
