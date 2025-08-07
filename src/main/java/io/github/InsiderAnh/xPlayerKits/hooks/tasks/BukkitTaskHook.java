package io.github.InsiderAnh.xPlayerKits.hooks.tasks;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.hooks.StellarTaskHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class BukkitTaskHook extends StellarTaskHook {

    public BukkitTaskHook(Runnable runnable) {
        super(runnable);
    }

    @Override
    public TaskCanceller runTask(Location location) {
        BukkitTask task = Bukkit.getScheduler().runTask(PlayerKits.getInstance(), runnable);
        return new TaskCanceller(task::cancel);
    }

    @Override
    public TaskCanceller runTask(Location location, long delay) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(PlayerKits.getInstance(), runnable, delay);
        return new TaskCanceller(task::cancel);
    }

    @Override
    public TaskCanceller runTask() {
        BukkitTask task = Bukkit.getScheduler().runTask(PlayerKits.getInstance(), runnable);
        return new TaskCanceller(task::cancel);
    }

    @Override
    public TaskCanceller runTask(long delay) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(PlayerKits.getInstance(), runnable, delay);
        return new TaskCanceller(task::cancel);
    }

    @Override
    public TaskCanceller runTaskLaterAsynchronously(long delay) {
        BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(PlayerKits.getInstance(), runnable, delay);
        return new TaskCanceller(task::cancel);
    }

    @Override
    public TaskCanceller runTaskTimer(long delay, long period) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(PlayerKits.getInstance(), runnable, delay, period);
        return new TaskCanceller(task::cancel);
    }

    @Override
    public TaskCanceller runTaskTimerAsynchronously(long delay, long period) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(PlayerKits.getInstance(), runnable, delay, period);
        return new TaskCanceller(task::cancel);
    }

}