package yamb.app.view;

import yamb.app.ApplicationContext;
import yamb.util.Disposable;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandProvider;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * @author manuel.laflamme
 * @since 24-Aug-2008
 */
public abstract class AbstractViewInternalFrame extends JInternalFrame
{
    protected final CommandGroupModel mCommandGroupModel;
    protected final ApplicationContext mAppContext;
    protected final CommandProvider mCommandProvider;
    protected final ActionModel mGlobalActionModel;
    protected final List<Disposable> mDisposables = new ArrayList<Disposable>();

    protected AbstractViewInternalFrame(CommandGroupModel aCommandGroupModel, ApplicationContext aAppContext, CommandProvider aCommandProvider, ActionModel aGlobalActionModel)
    {
        super("", //title
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable
        mCommandGroupModel = aCommandGroupModel;
        mAppContext = aAppContext;
        mCommandProvider = aCommandProvider;
        mGlobalActionModel = aGlobalActionModel;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addInternalFrameListener(new InternalFrameHandler());        
    }

    protected void addDisposable(Disposable aDisposable)
    {
        mDisposables.add(aDisposable);
    }

    public abstract ViewContext getViewContext();

    public ApplicationContext getAppContext()
    {
        return mAppContext;
    }

    public CommandGroupModel getCommandGroupModel()
    {
        return mCommandGroupModel;
    }

    public CommandProvider getCommandProvider()
    {
        return mCommandProvider;
    }

    public ActionModel getGlobalActionModel()
    {
        return mGlobalActionModel;
    }

    private class InternalFrameHandler extends InternalFrameAdapter
    {
        public void internalFrameActivated(InternalFrameEvent aEvent)
        {
            mAppContext.setActiveViewContext(getViewContext());
        }

        public void internalFrameClosed(InternalFrameEvent aEvent)
        {
            AbstractViewInternalFrame.this.removeInternalFrameListener(this);

            for (Disposable disposable : mDisposables)
            {
                disposable.dispose();
            }
            mDisposables.clear();

            if (mAppContext.getActiveViewContext() == getViewContext())
            {
                mAppContext.setActiveViewContext(null);
            }
        }
    }

}
