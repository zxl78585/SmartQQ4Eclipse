package cn.ieclipse.smartim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.scienjus.smartqq.QNUploader;

import cn.ieclipse.smartim.console.IMChatConsole;
import cn.ieclipse.smartim.model.IContact;
import cn.ieclipse.smartqq.console.QQChatConsole;

/**
 * The activator class controls the plug-in life cycle
 */
public class IMPlugin extends AbstractUIPlugin {
    
    // The plug-in ID
    public static final String PLUGIN_ID = "cn.ieclipse.smartqq"; //$NON-NLS-1$
    
    // The shared instance
    private static IMPlugin plugin;
    
    /**
     * The constructor
     */
    public IMPlugin() {
    }
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static IMPlugin getDefault() {
        if (plugin == null) {
            plugin = new IMPlugin();
        }
        return plugin;
    }
    
    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    // -------->
    private QNUploader uploader;
    public IConsole console;
    
    public IProxyService proxyService;
    
    public IProxyService getProxyService() {
        if (proxyService == null) {
            ServiceReference<IProxyService> ref = getBundle().getBundleContext()
                    .getServiceReference(IProxyService.class);
            proxyService = getBundle().getBundleContext().getService(ref);
        }
        
        return proxyService;
    }
    
    public QNUploader getUploader() {
        if (uploader == null) {
            uploader = new QNUploader();
        }
        return uploader;
    }
    
    public void log(String msg, Throwable e) {
        if (e == null) {
            IStatus info = new Status(IStatus.INFO, IMPlugin.PLUGIN_ID, msg);
            getLog().log(info);
        }
        else {
            IStatus info = new Status(IStatus.ERROR, IMPlugin.PLUGIN_ID, msg,
                    e);
            getLog().log(info);
        }
    }
    
    public void log(String msg) {
        log(msg, null);
    }
    
    public void start() {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
        manager.addConsoleListener(new IConsoleListener() {
            
            @Override
            public void consolesRemoved(IConsole[] consoles) {
                if (consoles != null) {
                    for (IConsole t : consoles) {
                        if (t == IMPlugin.getDefault().console) {
                            IMPlugin.getDefault().console = null;
                        }
                    }
                }
            }
            
            @Override
            public void consolesAdded(IConsole[] consoles) {
            }
        });
    }
    
    public IMChatConsole getChatConsole(String uin, boolean show) {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
                
        IConsole[] existing = manager.getConsoles();
        
        IMChatConsole console = null;
        for (int i = 0; i < existing.length; i++) {
            if (existing[i] instanceof IMChatConsole) {
                console = (IMChatConsole) existing[i];
                if (uin != null && uin.equals(console.getId())) {
                    break;
                }
                else {
                    console = null;
                }
            }
        }
        if (console != null && show) {
            manager.showConsoleView(console);
        }
        return console;
    }
    
    public <T extends IMChatConsole> T findConsole(Class<T> clazz, IContact obj,
            boolean add) {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
                
        IConsole[] existing = manager.getConsoles();
        
        T console = null;
        for (int i = 0; i < existing.length; i++) {
            if (existing[i].getClass().equals(clazz)) {
                T t = (T) existing[i];
                if (t.match(obj)) {
                    console = t;
                    break;
                }
            }
        }
        if (console == null && add) {
            try {
                console = clazz.getConstructor(IContact.class).newInstance(obj);
                manager.addConsoles(new IConsole[] { console });
            } catch (Exception e) {
                IStatus info = new Status(IStatus.ERROR, IMPlugin.PLUGIN_ID,
                        "无法创建" + clazz + e.toString());
                getLog().log(info);
            }
        }
        if (console != null && enable) {
            manager.showConsoleView(console);
            this.console = console;
        }
        return console;
    }
    
    public IMChatConsole nextConsole(IMChatConsole current, boolean next) {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
                
        IConsole[] existing = manager.getConsoles();
        
        IMChatConsole console = null;
        boolean find = false;
        if (next) {
            for (int i = 0; i < existing.length; i++) {
                if (find && existing[i].getClass().equals(current.getClass())) {
                    console = (IMChatConsole) existing[i];
                    break;
                }
                if (existing[i] == current) {
                    find = true;
                }
            }
        }
        else if (existing.length > 0) {
            for (int i = existing.length - 1; i >= 0; i--) {
                if (find && existing[i].getClass().equals(current.getClass())) {
                    console = (IMChatConsole) existing[i];
                    break;
                }
                if (existing[i] == current) {
                    find = true;
                }
            }
        }
        if (console != null) {
            manager.showConsoleView(console);
            this.console = console;
        }
        return console;
    }
    
    public void closeAllChat() {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
                
        IConsole[] existing = manager.getConsoles();
        List<IConsole> list = new ArrayList<>();
        for (int i = 0; i < existing.length; i++) {
            if (existing[i] instanceof IMChatConsole) {
                list.add(existing[i]);
            }
        }
        if (!list.isEmpty()) {
            manager.removeConsoles(list.toArray(new IConsole[list.size()]));
        }
        this.console = null;
    }
    
    public static void runOnUI(Runnable runnable) {
        Display display = Display.getDefault();
        if (display != null && !display.readAndDispatch()) {
            display.asyncExec(runnable);
        }
    }
    
    public boolean enable = true;
}
