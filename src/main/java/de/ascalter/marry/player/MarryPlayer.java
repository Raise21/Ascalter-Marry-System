package de.ascalter.marry.player;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public final class MarryPlayer {

  private final UUID uuid;
  private String name;
  private List<UUID> requests = new ArrayList<>();
  private String targetName;
  private boolean married;
}
