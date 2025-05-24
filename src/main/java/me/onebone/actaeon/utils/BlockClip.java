package me.onebone.actaeon.utils;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.math.Vector3;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class BlockClip {
    public static BlockClipIterator clip(Level level, Vector3 startVec, Vector3 endVec, boolean liquid, boolean ignoreBarrier, int maxIteration) {
        return new BlockClipIterator(level, startVec, endVec, liquid, ignoreBarrier, maxIteration);
    }

    public static Optional<MovingObjectPosition> clip(Level level, Vector3 startVec, Vector3 endVec, boolean liquid, boolean ignoreBarrier, int blockX, int blockY, int blockZ) {
        MovingObjectPosition hitResult = null;
        if (liquid) {
            Block extraBlock = level.getBlock(blockX, blockY, blockZ, false);
            if (extraBlock.isLiquid()) {
                // TODO 顺着水流往上爬
//                hitResult = extraBlock.clip(startVec, endVec, extraBlock::getCollisionBoundingBox);
            } else if (!extraBlock.isAir()) {
                hitResult = extraBlock.calculateIntercept(startVec, endVec);
            } else {
                Block block = level.getBlock(blockX, blockY, blockZ, false);
                if (block.isLiquid()) {
//                    hitResult = block.clip(startVec, endVec, block::getCollisionBoundingBox);
                } else if (ignoreBarrier && block.getId() == Block.BARRIER) {
                    hitResult = null;
                } else {
                    hitResult = block.calculateIntercept(startVec, endVec);
                }
            }
        } else {
            Block block = level.getBlock(blockX, blockY, blockZ, false);
            if (ignoreBarrier && block.getId() == Block.BARRIER) {
                hitResult = null;
            } else {
                hitResult = block.calculateIntercept(startVec, endVec);
            }
        }
        return Optional.ofNullable(hitResult);
    }

    public static class BlockClipIterator implements Iterator<MovingObjectPosition> {
        private final Level level;
        private final Vector3 startVec;
        private final Vector3 endVec;
        private final boolean liquid;
        private final boolean ignoreBarrier;
        private final int maxIteration;

        private final Vector3 currentVec;
        private int currentInteration;
        private final int blockStartX;
        private final int blockStartY;
        private final int blockStartZ;
        private final int blockEndX;
        private final int blockEndY;
        private final int blockEndZ;
        private int blockCurrentX;
        private int blockCurrentY;
        private int blockCurrentZ;

        private MovingObjectPosition pointer;
        private boolean finished;

        public Level getLevel() {
            return level;
        }

        public Vector3 getStartVec() {
            return startVec;
        }

        public Vector3 getEndVec() {
            return endVec;
        }

        public boolean isLiquid() {
            return liquid;
        }

        public boolean isIgnoreBarrier() {
            return ignoreBarrier;
        }

        public int getMaxIteration() {
            return maxIteration;
        }

        public Vector3 getCurrentVec() {
            return currentVec;
        }

        public int getCurrentInteration() {
            return currentInteration;
        }

        public int getBlockStartX() {
            return blockStartX;
        }

        public int getBlockStartY() {
            return blockStartY;
        }

        public int getBlockStartZ() {
            return blockStartZ;
        }

        public int getBlockEndX() {
            return blockEndX;
        }

        public int getBlockEndY() {
            return blockEndY;
        }

        public int getBlockEndZ() {
            return blockEndZ;
        }

        public int getBlockCurrentX() {
            return blockCurrentX;
        }

        public int getBlockCurrentY() {
            return blockCurrentY;
        }

        public int getBlockCurrentZ() {
            return blockCurrentZ;
        }

        public MovingObjectPosition getPointer() {
            return pointer;
        }

        public boolean isFinished() {
            return finished;
        }

        private BlockClipIterator(Level level, Vector3 startVec, Vector3 endVec, boolean liquid, boolean ignoreBarrier, int maxIteration) {
            this.level = level;
            this.startVec = startVec;
            this.endVec = endVec;
            this.liquid = liquid;
            this.ignoreBarrier = ignoreBarrier;
            this.maxIteration = maxIteration;

            this.currentVec = startVec.clone();
            this.currentInteration = 0;
            this.blockStartX = (int) Math.floor(startVec.x);
            this.blockStartY = (int) Math.floor(startVec.y);
            this.blockStartZ = (int) Math.floor(startVec.z);
            this.blockEndX = (int) Math.floor(endVec.x);
            this.blockEndY = (int) Math.floor(endVec.y);
            this.blockEndZ = (int) Math.floor(endVec.z);
            this.blockCurrentX = blockStartX;
            this.blockCurrentY = blockStartY;
            this.blockCurrentZ = blockStartZ;
        }

        private Optional<MovingObjectPosition> clip() {
            MovingObjectPosition hitResult = BlockClip.clip(level, currentVec, endVec, liquid, ignoreBarrier, blockCurrentX, blockCurrentY, blockCurrentZ).orElse(null);
            if (hitResult != null) return Optional.of(hitResult);

            while (currentInteration < maxIteration) {
                if (blockCurrentX == blockEndX && blockCurrentY == blockEndY && blockCurrentZ == blockEndZ)
                    return Optional.empty();
                step();
                hitResult = BlockClip.clip(level, currentVec, endVec, liquid, ignoreBarrier, blockCurrentX, blockCurrentY, blockCurrentZ).orElse(null);
                if (hitResult != null) return Optional.of(hitResult);
                currentInteration++;
            }

            return Optional.empty();
        }

        private void step() {
            double xClip = 0;
            double yClip = 0;
            double zClip = 0;
            boolean hasXClip = false;
            boolean hasYClip = false;
            boolean hasZClip = false;

            if (blockEndX > blockCurrentX) {
                hasXClip = true;
                xClip = blockCurrentX + 1;
            }
            if (blockEndX < blockCurrentX) {
                hasXClip = true;
                xClip = blockCurrentX;
            }
            if (blockEndY > blockCurrentY) {
                hasYClip = true;
                yClip = blockCurrentY + 1;
            }
            if (blockEndY < blockCurrentY) {
                hasYClip = true;
                yClip = blockCurrentY;
            }
            if (blockEndZ > blockCurrentZ) {
                hasZClip = true;
                zClip = blockCurrentZ + 1;
            }
            if (blockEndZ < blockCurrentZ) {
                hasZClip = true;
                zClip = blockCurrentZ;
            }

            double xDist = Integer.MAX_VALUE;
            double yDist = Integer.MAX_VALUE;
            double zDist = Integer.MAX_VALUE;
            double xd = endVec.x - currentVec.x;
            double yd = endVec.y - currentVec.y;
            double zd = endVec.z - currentVec.z;

            if (hasXClip) {
                xDist = (xClip - currentVec.x) / xd;
            }
            if (hasYClip) {
                yDist = (yClip - currentVec.y) / yd;
            }
            if (hasZClip) {
                zDist = (zClip - currentVec.z) / zd;
            }

            if (yDist <= xDist || zDist <= xDist) {
                if (zDist <= yDist) {
                    if (blockEndZ <= blockCurrentZ) {
                        --blockCurrentZ;
                    } else {
                        ++blockCurrentZ;
                    }

                    currentVec.x = xd * zDist + currentVec.x;
                    currentVec.y = yd * zDist + currentVec.y;
                    currentVec.z = zClip;
                } else {
                    if (blockEndY <= blockCurrentY) {
                        --blockCurrentY;
                    } else {
                        ++blockCurrentY;
                    }

                    currentVec.x = xd * yDist + currentVec.x;
                    currentVec.y = yClip;
                    currentVec.z = zd * yDist + currentVec.z;
                }
            } else {
                if (blockEndX <= blockCurrentX) {
                    --blockCurrentX;
                } else {
                    ++blockCurrentX;
                }

                currentVec.x = xClip;
                currentVec.y = yd * xDist + currentVec.y;
                currentVec.z = zd * xDist + currentVec.z;
            }
        }

        @Override
        public boolean hasNext() {
            if (pointer != null) return true;
            if (finished) return false;
            MovingObjectPosition result = clip().orElse(null);
            if (result == null) {
                finished = true;
                return false;
            } else {
                pointer = result;
                return true;
            }
        }

        @Override
        public MovingObjectPosition next() {
            if (!hasNext()) throw new NoSuchElementException();
            MovingObjectPosition result = pointer;
            pointer = null;
            return result;
        }
    }
}
