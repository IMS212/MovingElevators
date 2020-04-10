package com.supermartijn642.movingelevators.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

/**
 * Created 4/3/2020 by SuperMartijn642
 */
public class ElevatorSizeSlider extends GuiButton {

    private float sliderValue;
    public boolean dragging;
    private final Consumer<ElevatorSizeSlider> onChange;

    public ElevatorSizeSlider(int xIn, int yIn, int widthIn, int heightIn, int currentValue, Consumer<ElevatorSizeSlider> onChange){
        super(0, xIn, yIn, widthIn, heightIn, "");
        this.sliderValue = ((currentValue - 1) / 2) / 4f;
        this.onChange = onChange;
        this.updateMessage();
    }

    protected void updateMessage(){
        int val = this.getValue();
        this.displayString = I18n.format("movingelevators.platform.size").replace("$number$", val + "x" + val);
    }

    public int getValue(){
        return Math.round(this.sliderValue * 4) * 2 + 1;
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver){
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY){
        if(this.visible){
            if(this.dragging){
                this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
                this.sliderValue = ((this.getValue() - 1) / 2) / 4f;
                this.updateMessage();
                this.onChange.accept(this);
            }

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY){
        if(super.mousePressed(mc, mouseX, mouseY)){
            this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            this.sliderValue = ((this.getValue() - 1) / 2) / 4f;
            this.updateMessage();
            this.onChange.accept(this);
            this.dragging = true;
            return true;
        }else{
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY){
        this.dragging = false;
    }
}
