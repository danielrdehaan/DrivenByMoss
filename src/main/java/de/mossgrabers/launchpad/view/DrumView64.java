// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.launchpad.view;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.view.AbstractDrumView64;
import de.mossgrabers.launchpad.LaunchpadConfiguration;
import de.mossgrabers.launchpad.controller.LaunchpadColors;
import de.mossgrabers.launchpad.controller.LaunchpadControlSurface;


/**
 * The Drum 64 view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DrumView64 extends AbstractDrumView64<LaunchpadControlSurface, LaunchpadConfiguration>
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public DrumView64 (final LaunchpadControlSurface surface, final Model model)
    {
        super (surface, model);
    }


    /** {@inheritDoc} */
    @Override
    protected void handleDeleteButton (final int playedPad)
    {
        this.surface.setButtonConsumed (this.surface.getDeleteButtonId ());
        ((DrumView) this.surface.getViewManager ().getView (Views.VIEW_DRUM)).getClip ().clearRow (this.offsetY + playedPad);
    }


    /** {@inheritDoc} */
    @Override
    public void updateSceneButtons ()
    {
        for (int i = 0; i < 8; i++)
            this.surface.setButton (this.surface.getSceneButton (i), LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    }
}