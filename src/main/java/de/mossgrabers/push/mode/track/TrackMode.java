// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.push.mode.track;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.controller.ValueChanger;
import de.mossgrabers.framework.controller.display.Display;
import de.mossgrabers.framework.controller.display.Format;
import de.mossgrabers.framework.daw.AbstractTrackBankProxy;
import de.mossgrabers.framework.daw.EffectTrackBankProxy;
import de.mossgrabers.framework.daw.TrackBankProxy;
import de.mossgrabers.framework.daw.data.SendData;
import de.mossgrabers.framework.daw.data.TrackData;
import de.mossgrabers.push.PushConfiguration;
import de.mossgrabers.push.controller.DisplayMessage;
import de.mossgrabers.push.controller.PushControlSurface;
import de.mossgrabers.push.controller.PushDisplay;


/**
 * Mode for editing a track parameters.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class TrackMode extends AbstractTrackMode
{
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public TrackMode (final PushControlSurface surface, final Model model)
    {
        super (surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final TrackData selectedTrack = tb.getSelectedTrack ();
        if (selectedTrack == null)
            return;

        switch (index)
        {
            case 0:
                tb.changeVolume (selectedTrack.getIndex (), value);
                return;
            case 1:
                tb.changePan (selectedTrack.getIndex (), value);
                return;
        }

        final PushConfiguration config = this.surface.getConfiguration ();
        if (this.isPush2)
        {
            switch (index)
            {
                case 2:
                    tb.changeCrossfadeModeAsNumber (selectedTrack.getIndex (), value);
                    break;
                case 3:
                    break;
                default:
                    final int sendOffset = config.isSendsAreToggled () ? 0 : 4;
                    ((TrackBankProxy) tb).changeSend (selectedTrack.getIndex (), index - sendOffset, value);
                    break;
            }
            return;
        }

        switch (index)
        {
            case 2:
                if (config.isDisplayCrossfader ())
                    tb.changeCrossfadeModeAsNumber (selectedTrack.getIndex (), value);
                else
                    ((TrackBankProxy) tb).changeSend (selectedTrack.getIndex (), 0, value);
                break;
            default:
                ((TrackBankProxy) tb).changeSend (selectedTrack.getIndex (), index - (config.isDisplayCrossfader () ? 3 : 2), value);
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnobTouch (final int index, final boolean isTouched)
    {
        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final TrackData selectedTrack = tb.getSelectedTrack ();
        if (selectedTrack == null)
            return;

        this.isKnobTouched[index] = isTouched;

        final PushConfiguration config = this.surface.getConfiguration ();
        if (this.isPush2)
        {
            if (isTouched)
            {
                if (this.surface.isDeletePressed ())
                {
                    this.surface.setButtonConsumed (PushControlSurface.PUSH_BUTTON_DELETE);
                    switch (index)
                    {
                        case 0:
                            tb.resetVolume (selectedTrack.getIndex ());
                            break;
                        case 1:
                            tb.resetPan (selectedTrack.getIndex ());
                            break;
                        case 2:
                            tb.setCrossfadeMode (selectedTrack.getIndex (), "AB");
                            break;
                        case 3:
                            // Not used
                            break;
                        default:
                            ((TrackBankProxy) tb).resetSend (selectedTrack.getIndex (), index - 4);
                            break;
                    }
                    return;
                }

                final PushDisplay display = (PushDisplay) this.surface.getDisplay ();
                switch (index)
                {
                    case 0:
                        display.notify ("Volume: " + selectedTrack.getVolumeStr (8));
                        break;
                    case 1:
                        display.notify ("Pan: " + selectedTrack.getPanStr (8));
                        break;
                    case 2:
                        display.notify ("Crossfader: " + selectedTrack.getCrossfadeMode ());
                        break;
                    case 3:
                        // Not used
                        break;
                    default:
                        final int sendIndex = index - 4;
                        final EffectTrackBankProxy fxTrackBank = this.model.getEffectTrackBank ();
                        final String name = fxTrackBank == null ? selectedTrack.getSends ()[sendIndex].getName () : fxTrackBank.getTrack (sendIndex).getName ();
                        if (name.length () > 0)
                            display.notify ("Send " + name + ": " + selectedTrack.getSends ()[sendIndex].getDisplayedValue (8));
                        break;
                }
            }

            switch (index)
            {
                case 0:
                    tb.touchVolume (selectedTrack.getIndex (), isTouched);
                    break;
                case 1:
                    tb.touchPan (selectedTrack.getIndex (), isTouched);
                    break;
                case 2:
                case 3:
                    // Not used
                    break;
                default:
                    final int sendIndex = index - 4;
                    ((TrackBankProxy) tb).touchSend (selectedTrack.getIndex (), sendIndex, isTouched);
                    break;
            }

            this.checkStopAutomationOnKnobRelease (isTouched);
            return;
        }

        if (isTouched)
        {
            if (this.surface.isDeletePressed ())
            {
                this.surface.setButtonConsumed (PushControlSurface.PUSH_BUTTON_DELETE);
                switch (index)
                {
                    case 0:
                        tb.resetVolume (selectedTrack.getIndex ());
                        break;
                    case 1:
                        tb.resetPan (selectedTrack.getIndex ());
                        break;
                    case 2:
                        if (config.isDisplayCrossfader ())
                            tb.setCrossfadeMode (selectedTrack.getIndex (), "AB");
                        else
                            ((TrackBankProxy) tb).resetSend (selectedTrack.getIndex (), 0);
                        break;
                    default:
                        ((TrackBankProxy) tb).resetSend (selectedTrack.getIndex (), index - (config.isDisplayCrossfader () ? 3 : 2));
                        break;
                }
                return;
            }

            final PushDisplay display = (PushDisplay) this.surface.getDisplay ();
            switch (index)
            {
                case 0:
                    display.notify ("Volume: " + selectedTrack.getVolumeStr (8));
                    break;
                case 1:
                    display.notify ("Pan: " + selectedTrack.getPanStr (8));
                    break;
                case 2:
                    if (config.isDisplayCrossfader ())
                        display.notify ("Crossfader: " + selectedTrack.getCrossfadeMode ());
                    else
                    {
                        final int sendIndex = 0;
                        final EffectTrackBankProxy fxTrackBank = this.model.getEffectTrackBank ();
                        final String name = fxTrackBank == null ? selectedTrack.getSends ()[sendIndex].getName () : fxTrackBank.getTrack (sendIndex).getName ();
                        if (name.length () > 0)
                            display.notify ("Send " + name + ": " + selectedTrack.getSends ()[sendIndex].getDisplayedValue (8));
                    }
                    break;
                default:
                    final int sendIndex = index - (config.isDisplayCrossfader () ? 3 : 2);
                    final EffectTrackBankProxy fxTrackBank = this.model.getEffectTrackBank ();
                    final String name = fxTrackBank == null ? selectedTrack.getSends ()[sendIndex].getName () : fxTrackBank.getTrack (sendIndex).getName ();
                    if (name.length () > 0)
                        display.notify ("Send " + name + ": " + selectedTrack.getSends ()[sendIndex].getDisplayedValue (8));
                    break;
            }
        }

        switch (index)
        {
            case 0:
                tb.touchVolume (selectedTrack.getIndex (), isTouched);
                break;
            case 1:
                tb.touchPan (selectedTrack.getIndex (), isTouched);
                break;
            case 2:
                if (!config.isDisplayCrossfader ())
                    ((TrackBankProxy) tb).touchSend (selectedTrack.getIndex (), 0, isTouched);
                break;
            default:
                final int sendIndex = index - (config.isDisplayCrossfader () ? 3 : 2);
                ((TrackBankProxy) tb).touchSend (selectedTrack.getIndex (), sendIndex, isTouched);
                break;
        }

        this.checkStopAutomationOnKnobRelease (isTouched);
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 ()
    {
        final Display d = this.surface.getDisplay ().clear ();
        final AbstractTrackBankProxy currentTrackBank = this.model.getCurrentTrackBank ();
        final TrackData t = currentTrackBank.getSelectedTrack ();
        if (t == null)
            d.setRow (1, "                     Please selecta track...                        ").done (0).done (2);
        else
        {
            final PushConfiguration config = this.surface.getConfiguration ();
            d.setCell (0, 0, "Volume").setCell (1, 0, t.getVolumeStr (8)).setCell (2, 0, config.isEnableVUMeters () ? t.getVu () : t.getVolume (), Format.FORMAT_VALUE);
            d.setCell (0, 1, "Pan").setCell (1, 1, t.getPanStr (8)).setCell (2, 1, t.getPan (), Format.FORMAT_PAN);

            int sendStart = 2;
            int sendCount = 6;
            if (config.isDisplayCrossfader ())
            {
                sendStart = 3;
                sendCount = 5;
                final String crossfadeMode = t.getCrossfadeMode ();
                final int upperBound = this.model.getValueChanger ().getUpperBound ();
                d.setCell (0, 2, "Crossfdr").setCell (1, 2, "A".equals (crossfadeMode) ? "A" : "B".equals (crossfadeMode) ? "       B" : "   <> ");
                d.setCell (2, 2, "A".equals (crossfadeMode) ? 0 : "B".equals (crossfadeMode) ? upperBound : upperBound / 2, Format.FORMAT_PAN);
            }
            final boolean isEffectTrackBankActive = this.model.isEffectTrackBankActive ();
            for (int i = 0; i < sendCount; i++)
            {
                final int pos = sendStart + i;
                if (!isEffectTrackBankActive)
                {
                    final SendData sendData = t.getSends ()[i];
                    if (sendData.doesExist ())
                        d.setCell (0, pos, sendData.getName ()).setCell (1, pos, sendData.getDisplayedValue (8)).setCell (2, pos, sendData.getValue (), Format.FORMAT_VALUE);
                }
            }
            d.done (0).done (1).done (2);
        }

        this.drawRow4 ();
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 ()
    {
        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final TrackData selectedTrack = tb.getSelectedTrack ();

        // Get the index at which to draw the Sends element
        final int selectedIndex = selectedTrack == null ? -1 : selectedTrack.getIndex ();
        int sendsIndex = selectedTrack == null || this.model.isEffectTrackBankActive () ? -1 : selectedTrack.getIndex () + 1;
        if (sendsIndex == 8)
            sendsIndex = 6;

        this.updateTrackMenu ();

        final PushConfiguration config = this.surface.getConfiguration ();
        final DisplayMessage message = ((PushDisplay) this.surface.getDisplay ()).createMessage ();
        for (int i = 0; i < 8; i++)
        {
            final TrackData t = tb.getTrack (i);

            if (sendsIndex == i)
                message.addByte (DisplayMessage.GRID_ELEMENT_CHANNEL_SENDS);
            else
                message.addByte (t.isSelected () ? DisplayMessage.GRID_ELEMENT_CHANNEL_ALL : DisplayMessage.GRID_ELEMENT_CHANNEL_SELECTION);

            // The menu item
            if (config.isMuteLongPressed () || config.isMuteSoloLocked () && config.isMuteState ())
            {
                message.addString (t.doesExist () ? "Mute" : "");
                message.addBoolean (t.isMute ());
            }
            else if (config.isSoloLongPressed () || config.isMuteSoloLocked () && config.isSoloState ())
            {
                message.addString (t.doesExist () ? "Solo" : "");
                message.addBoolean (t.isSolo ());
            }
            else
            {
                message.addString (this.menu[i]);
                message.addBoolean (false);
            }

            // Channel info
            message.addString (t.doesExist () ? t.getName () : "");
            message.addString (t.getType ());
            message.addColor (tb.getTrackColorEntry (i));
            message.addByte (t.isSelected () ? 1 : 0);

            final ValueChanger valueChanger = this.model.getValueChanger ();
            if (t.isSelected ())
            {
                message.addInteger (valueChanger.toDisplayValue (t.getVolume ()));
                message.addInteger (valueChanger.toDisplayValue (t.getModulatedVolume ()));
                message.addString (this.isKnobTouched[0] ? t.getVolumeStr (8) : "");
                message.addInteger (valueChanger.toDisplayValue (t.getPan ()));
                message.addInteger (valueChanger.toDisplayValue (t.getModulatedPan ()));
                message.addString (this.isKnobTouched[1] ? t.getPanStr (8) : "");
                message.addInteger (valueChanger.toDisplayValue (config.isEnableVUMeters () ? t.getVu () : 0));
                message.addBoolean (t.isMute ());
                message.addBoolean (t.isSolo ());
                message.addBoolean (t.isRecarm ());
                message.addByte ("A".equals (t.getCrossfadeMode ()) ? 0 : "B".equals (t.getCrossfadeMode ()) ? 2 : 1);
            }
            else if (sendsIndex == i)
            {
                final EffectTrackBankProxy fxTrackBank = this.model.getEffectTrackBank ();
                final TrackData selTrack = tb.getTrack (selectedIndex);
                for (int j = 0; j < 4; j++)
                {
                    final int sendOffset = config.isSendsAreToggled () ? 4 : 0;
                    final int sendPos = sendOffset + j;
                    final SendData send = selTrack.getSends ()[sendPos];
                    message.addString (fxTrackBank == null ? send.getName () : fxTrackBank.getTrack (sendPos).getName ());
                    message.addString (send.doesExist () && this.isKnobTouched[4 + j] ? send.getDisplayedValue (8) : "");
                    message.addInteger (valueChanger.toDisplayValue (send.doesExist () ? send.getValue () : 0));
                    message.addInteger (valueChanger.toDisplayValue (send.doesExist () ? send.getModulatedValue () : 0));
                    message.addByte (1);
                }
                // Signal Track mode
                message.addBoolean (true);
            }
        }

        message.send ();
    }
}