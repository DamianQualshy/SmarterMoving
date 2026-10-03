package net.smart.moving.model;

/** State attached to the actual vanilla player and armor model instances. */
public interface IModelPlayer {
    SMModel getMovingModel();
    SMModel getMovingModelIfPresent();
}
