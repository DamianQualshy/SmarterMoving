package net.smart.moving.render;

import net.minecraft.entity.player.EntityPlayer;

/** The movement samples needed by Smart Moving's player poses. Owned by each client player. */
public final class MovingStatistics {
    private static final int HISTORY = 10;
    private final EntityPlayer player;
    private final Sample[] history = new Sample[HISTORY];
    private int index = -1;
    private float tickDistance;
    public int ticksRiding;
    public float prevHorizontalAngle = Float.NaN;

    public MovingStatistics(EntityPlayer player) { this.player = player; }

    public void calculate(boolean remote) {
        double dx = player.posX - player.prevPosX;
        double dy = player.posY - player.prevPosY;
        double dz = player.posZ - player.prevPosZ;
        Sample previous = index < 0 ? null : history[index];
        index = (index + 1) % HISTORY;
        Sample sample = history[index];
        if (sample == null) sample = history[index] = new Sample();
        sample.begin(previous);
        sample.horizontal.advance((float) Math.sqrt(dx * dx + dz * dz));
        sample.vertical.advance((float) Math.abs(dy));
        tickDistance = sample.all.advance((float) Math.sqrt(dx * dx + dy * dy + dz * dz));
        if (!remote) {
            player.prevLimbSwingAmount = sample.horizontal.previous;
            player.limbSwingAmount = sample.horizontal.current;
            player.limbSwing = sample.horizontal.total;
        }
    }

    public void calculateRidden() { ticksRiding++; }
    public float getTickDistance() { return tickDistance; }
    public float getTotalVerticalDistance(float partial) { return current().vertical.total(partial); }
    public float getCurrentVerticalSpeed(float partial) { return current().vertical.speed(partial); }
    public float getTotalDistance(float partial) { return current().all.total(partial); }
    public float getCurrentSpeed(float partial) { return current().all.speed(partial); }

    public float getCurrentHorizontalSpeedFlattened(float partial) {
        if (index < 0) return Float.NaN;
        history[index].ready = true;
        float sum = 0;
        int count = 0;
        for (int i = 0; i < HISTORY; i++) {
            Sample sample = history[(index - i + HISTORY) % HISTORY];
            if (sample == null || !sample.ready) break;
            sum += sample.horizontal.speed(partial);
            count++;
        }
        return count == 0 ? Float.NaN : sum / count;
    }

    private Sample current() { return index < 0 ? Sample.EMPTY : history[index]; }

    private static final class Sample {
        private static final Sample EMPTY = new Sample();
        final Axis horizontal = new Axis();
        final Axis vertical = new Axis();
        final Axis all = new Axis();
        boolean ready;

        void begin(Sample previous) {
            ready = false;
            horizontal.begin(previous == null ? null : previous.horizontal);
            vertical.begin(previous == null ? null : previous.vertical);
            all.begin(previous == null ? null : previous.all);
        }
    }

    private static final class Axis {
        float previous;
        float current;
        float total;

        void begin(Axis old) {
            previous = old == null ? 0 : old.current;
            current = previous;
            total = old == null ? 0 : old.total;
        }

        float advance(float distance) {
            distance *= 4F;
            current += (distance - current) * 0.4F;
            total += current;
            return distance;
        }

        float speed(float partial) { return Math.min(1F, previous + (current - previous) * partial); }
        float total(float partial) { return total - current * (1F - partial); }
    }
}
