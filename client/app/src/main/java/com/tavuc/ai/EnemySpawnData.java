package com.tavuc.ai;

import com.tavuc.models.entities.enemies.EnemyType;

/** Data describing enemies to spawn in a wave. */
public record EnemySpawnData(EnemyType type, int count) {}
