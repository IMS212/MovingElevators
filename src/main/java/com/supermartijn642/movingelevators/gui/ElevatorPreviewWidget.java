package com.supermartijn642.movingelevators.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.widget.Widget;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.gui.preview.ElevatorPreviewRenderer;
import com.supermartijn642.movingelevators.gui.preview.WorldBlockCapture;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Created 10/02/2022 by SuperMartijn642
 */
public class ElevatorPreviewWidget extends Widget {

    private final Supplier<ControllerBlockEntity> elevatorEntity;
    private final Supplier<BlockPos> previewSizeIncrease;
    private final Supplier<BlockPos> previewOffset;
    private final Supplier<Float> guiLeft, guiTop;

    private float yaw = 20, pitch = 30;
    private boolean dragging = false;
    private int mouseStartX, mouseStartY;

    public ElevatorPreviewWidget(int x, int y, int width, int height,
                                 Supplier<ControllerBlockEntity> elevatorEntity,
                                 Supplier<BlockPos> previewSizeIncrease,
                                 Supplier<BlockPos> previewOffset,
                                 Supplier<Float> guiLeft,
                                 Supplier<Float> guiTop){
        super(x, y, width, height);
        this.elevatorEntity = elevatorEntity;
        this.previewSizeIncrease = previewSizeIncrease;
        this.previewOffset = previewOffset;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return null;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        // Update the rotation
        if(this.dragging){
            this.yaw += (mouseX - this.mouseStartX) / 100d * 360;
            this.pitch += (mouseY - this.mouseStartY) / 100d * 360;
            this.mouseStartX = mouseX;
            this.mouseStartY = mouseY;
        }

        // Create capture of the elevator cabin
        ControllerBlockEntity elevatorEntity = this.elevatorEntity.get();
        ElevatorGroup group = elevatorEntity.getGroup();
        BlockPos anchorPos = group.getCageAnchorBlockPos(elevatorEntity.getBlockPos().getY());

        WorldBlockCapture capture = new WorldBlockCapture(group.world);

        capture.putBlock(elevatorEntity.getBlockPos(), elevatorEntity.getBlockPos());
        for(int x = 0; x < group.getCageSizeX(); x++){
            for(int y = 0; y < group.getCageSizeY(); y++){
                for(int z = 0; z < group.getCageSizeZ(); z++){
                    BlockPos pos = anchorPos.offset(x, y, z);
                    capture.putBlock(pos, pos);
                }
            }
        }

        // Get the bounding boxes
        AxisAlignedBB cabinBox = new AxisAlignedBB(anchorPos, anchorPos.offset(group.getCageSizeX(), group.getCageSizeY(), group.getCageSizeZ())).inflate(0.1);
        BlockPos previewSizeIncrease = this.previewSizeIncrease.get();
        BlockPos previewOffset = this.previewOffset.get();
        AxisAlignedBB previewBox = null;
        if(!previewSizeIncrease.equals(BlockPos.ZERO) || !previewOffset.equals(BlockPos.ZERO)){
            int cabinWidth = group.getCageWidth() + previewSizeIncrease.getX(), cabinDepth = group.getCageDepth() + previewSizeIncrease.getZ(), cabinHeight = group.getCageHeight() + previewSizeIncrease.getY();
            int sideOffset = group.getCageSideOffset() + previewOffset.getX(), depthOffset = group.getCageDepthOffset() + previewOffset.getZ(), heightOffset = group.getCageHeightOffset() + previewOffset.getY();
            if(sideOffset > 2 + (group.facing == Direction.NORTH || group.facing == Direction.WEST ? (cabinWidth - 1) / 2 : (int)Math.ceil((cabinWidth - 1) / 2f)))
                sideOffset = 2 + (group.facing == Direction.NORTH || group.facing == Direction.WEST ? (cabinWidth - 1) / 2 : (int)Math.ceil((cabinWidth - 1) / 2f));
            else if(sideOffset < -2 - (group.facing == Direction.NORTH || group.facing == Direction.WEST ? (int)Math.ceil((cabinWidth - 1) / 2f) : (cabinWidth - 1) / 2))
                sideOffset = -2 - (group.facing == Direction.NORTH || group.facing == Direction.WEST ? (int)Math.ceil((cabinWidth - 1) / 2f) : (cabinWidth - 1) / 2);
            if(heightOffset < -cabinHeight)
                heightOffset = -cabinHeight;
            int anchorX = 0, anchorY = elevatorEntity.getBlockPos().getY(), anchorZ = 0;
            if(group.facing == Direction.NORTH){
                anchorX = group.x - cabinWidth / 2 - sideOffset;
                anchorZ = group.z - cabinDepth - depthOffset;
            }else if(group.facing == Direction.SOUTH){
                anchorX = group.x - cabinWidth / 2 + sideOffset;
                anchorZ = group.z + 1 + depthOffset;
            }else if(group.facing == Direction.WEST){
                anchorX = group.x - cabinDepth - depthOffset;
                anchorZ = group.z - cabinWidth / 2 + sideOffset;
            }else if(group.facing == Direction.EAST){
                anchorX = group.x + 1 + depthOffset;
                anchorZ = group.z - cabinWidth / 2 - sideOffset;
            }
            anchorY += heightOffset;
            previewBox = new AxisAlignedBB(anchorX, anchorY, anchorZ, anchorX + (group.facing.getAxis() == Direction.Axis.X ? cabinDepth : cabinWidth), anchorY + cabinHeight, anchorZ + (group.facing.getAxis() == Direction.Axis.Z ? cabinDepth : cabinWidth)).inflate(0.1);
        }

        // Render the preview
        ElevatorPreviewRenderer.renderPreview(capture, cabinBox, previewBox, this.x + this.width / 2f + this.guiLeft.get(), this.y + this.height / 2f + this.guiTop.get(), Math.min(this.width, this.height), this.yaw + group.facing.toYRot(), this.pitch, false);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button){
        if(mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height){
            this.dragging = true;
            this.mouseStartX = mouseX;
            this.mouseStartY = mouseY;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button){
        this.dragging = false;
    }
}
