package com.github.plateofpasta.chunkclaimfabric.util;

import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tag builder helper for simplifying the build process.
 *
 * @param <T> Tag type to add.
 */
public class TagBuilderHelper<T> {
  private final String source;
  private final Registry<T> registry;
  private final Tag.Builder builder;

  /**
   * @param source Source "ID" of this tag.
   * @param registry Global registry associated with the entry type for this tag.
   */
  public TagBuilderHelper(String source, Registry<T> registry) {
    this.source = source;
    this.registry = registry;
    this.builder = new Tag.Builder();
  }

  /**
   * Builder function for adding entries to this tag
   *
   * @param entries Entries to add to this.
   * @return {@code this} builder.
   */
  @SafeVarargs
  public final TagBuilderHelper<T> add(T... entries) {
    Stream.of(entries)
        .map(this.registry::getId)
        .forEach((identifier) -> this.builder.add(identifier, this.source));
    return this;
  }

  /**
   * Builder function for adding the contents of existing tags to this.
   *
   * @param tagEntries Tags to add to this.
   * @return {@code this} builder.
   */
  @SafeVarargs
  public final TagBuilderHelper<T> add(Tag.Identified<T>... tagEntries) {
    Stream.of(tagEntries)
        .map(Tag::values)
        .flatMap(List::stream)
        .map(this.registry::getId)
        .forEach(identifier -> this.builder.add(identifier, this.source));
    return this;
  }

  /**
   * Builds the tag from the builder.
   *
   * @return Built tag.
   * @throws RuntimeException If an error occurred during tag building.
   */
  public Tag<T> build() throws RuntimeException {
    // Omit tagGetter, since the add methods should always resolve to an ObjectEntry.
    Optional<Tag<T>> tag = this.builder.build(null, registry::get);
    if (tag.isPresent()) {
      return tag.get();
    } else {
      throw new RuntimeException("Error building tag for ChunkClaim:" + source);
    }
  }
}
