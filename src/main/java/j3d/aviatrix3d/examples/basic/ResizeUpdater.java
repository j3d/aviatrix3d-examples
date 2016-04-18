/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package j3d.aviatrix3d.examples.basic;

import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

import org.j3d.aviatrix3d.ApplicationUpdateObserver;

/**
 * Internal class that only runs the update handler for the resizer
 *
 * @author justin
 */
class ResizeUpdater implements ApplicationUpdateObserver
{
    private ViewportResizeManager resizeManager;

    ResizeUpdater(ViewportResizeManager manager)
    {
        resizeManager = manager;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    @Override
    public void updateSceneGraph()
    {
        resizeManager.sendResizeUpdates();
    }

    @Override
    public void appShutdown()
    {
        // Do nothing
    }
}
