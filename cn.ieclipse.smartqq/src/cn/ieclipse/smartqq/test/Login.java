/**
 * 
 */
package cn.ieclipse.smartqq.test;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cn.ieclipse.smartim.IMPlugin;
import cn.ieclipse.smartim.dialogs.LoginDialog;

/**
 * @author Jamling
 *         
 */
public class Login {
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        LoginDialog dialog = new LoginDialog(new Shell());
        dialog.open();
        IMPlugin.getDefault().start();
        Display display = Display.getDefault();
        while (!display.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
    
}
