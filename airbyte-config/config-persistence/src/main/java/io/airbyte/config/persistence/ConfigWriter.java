/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.config.persistence;

import static io.airbyte.db.instance.configs.jooq.generated.Tables.ACTOR_DEFINITION_VERSION;

import io.airbyte.commons.enums.Enums;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.StandardDestinationDefinition;
import io.airbyte.config.StandardSourceDefinition;
import io.airbyte.db.instance.configs.jooq.generated.Tables;
import io.airbyte.db.instance.configs.jooq.generated.enums.ActorType;
import io.airbyte.db.instance.configs.jooq.generated.enums.SourceType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;

/**
 * This class can be used to store DB queries for persisting configs that we may want to reuse
 * across this package.
 * <p>
 * Currently this class is used to move write queries out of {@link ConfigPersistence} so that they
 * can be reused/composed in {@link ConfigRepository}.
 */
@SuppressWarnings("PMD.CognitiveComplexity")
public class ConfigWriter {

  static int writeSourceDefinitionImageTag(final List<UUID> sourceDefinitionIds, final String targetImageTag, final DSLContext ctx) {
    final OffsetDateTime timestamp = OffsetDateTime.now();

    // We are updating the same version since connector builder projects have a different concept of
    // versioning
    return ctx.update(ACTOR_DEFINITION_VERSION).set(ACTOR_DEFINITION_VERSION.DOCKER_IMAGE_TAG, targetImageTag)
        .set(ACTOR_DEFINITION_VERSION.UPDATED_AT, timestamp)
        .where(
            ACTOR_DEFINITION_VERSION.ACTOR_DEFINITION_ID.in(sourceDefinitionIds).andNot(ACTOR_DEFINITION_VERSION.DOCKER_IMAGE_TAG.eq(targetImageTag)))
        .execute();
  }

  static void writeStandardSourceDefinition(final List<StandardSourceDefinition> configs, final DSLContext ctx) {
    final OffsetDateTime timestamp = OffsetDateTime.now();
    configs.forEach((standardSourceDefinition) -> {
      final boolean isExistingConfig = ctx.fetchExists(DSL.select()
          .from(Tables.ACTOR_DEFINITION)
          .where(Tables.ACTOR_DEFINITION.ID.eq(standardSourceDefinition.getSourceDefinitionId())));

      if (isExistingConfig) {
        ctx.update(Tables.ACTOR_DEFINITION)
            .set(Tables.ACTOR_DEFINITION.ID, standardSourceDefinition.getSourceDefinitionId())
            .set(Tables.ACTOR_DEFINITION.NAME, standardSourceDefinition.getName())
            .set(Tables.ACTOR_DEFINITION.ICON, standardSourceDefinition.getIcon())
            .set(Tables.ACTOR_DEFINITION.ACTOR_TYPE, ActorType.source)
            .set(Tables.ACTOR_DEFINITION.SOURCE_TYPE,
                standardSourceDefinition.getSourceType() == null ? null
                    : Enums.toEnum(standardSourceDefinition.getSourceType().value(),
                        SourceType.class).orElseThrow())
            .set(Tables.ACTOR_DEFINITION.TOMBSTONE, standardSourceDefinition.getTombstone())
            .set(Tables.ACTOR_DEFINITION.PUBLIC, standardSourceDefinition.getPublic())
            .set(Tables.ACTOR_DEFINITION.CUSTOM, standardSourceDefinition.getCustom())
            .set(Tables.ACTOR_DEFINITION.RESOURCE_REQUIREMENTS,
                standardSourceDefinition.getResourceRequirements() == null ? null
                    : JSONB.valueOf(Jsons.serialize(standardSourceDefinition.getResourceRequirements())))
            .set(Tables.ACTOR_DEFINITION.UPDATED_AT, timestamp)
            .set(Tables.ACTOR_DEFINITION.MAX_SECONDS_BETWEEN_MESSAGES,
                standardSourceDefinition.getMaxSecondsBetweenMessages() == null ? null
                    : standardSourceDefinition.getMaxSecondsBetweenMessages().intValue())
            .where(Tables.ACTOR_DEFINITION.ID.eq(standardSourceDefinition.getSourceDefinitionId()))
            .execute();

      } else {
        ctx.insertInto(Tables.ACTOR_DEFINITION)
            .set(Tables.ACTOR_DEFINITION.ID, standardSourceDefinition.getSourceDefinitionId())
            .set(Tables.ACTOR_DEFINITION.NAME, standardSourceDefinition.getName())
            .set(Tables.ACTOR_DEFINITION.ICON, standardSourceDefinition.getIcon())
            .set(Tables.ACTOR_DEFINITION.ACTOR_TYPE, ActorType.source)
            .set(Tables.ACTOR_DEFINITION.SOURCE_TYPE,
                standardSourceDefinition.getSourceType() == null ? null
                    : Enums.toEnum(standardSourceDefinition.getSourceType().value(),
                        SourceType.class).orElseThrow())
            .set(Tables.ACTOR_DEFINITION.TOMBSTONE, standardSourceDefinition.getTombstone() != null && standardSourceDefinition.getTombstone())
            .set(Tables.ACTOR_DEFINITION.PUBLIC, standardSourceDefinition.getPublic())
            .set(Tables.ACTOR_DEFINITION.CUSTOM, standardSourceDefinition.getCustom())
            .set(Tables.ACTOR_DEFINITION.RESOURCE_REQUIREMENTS,
                standardSourceDefinition.getResourceRequirements() == null ? null
                    : JSONB.valueOf(Jsons.serialize(standardSourceDefinition.getResourceRequirements())))
            .set(Tables.ACTOR_DEFINITION.CREATED_AT, timestamp)
            .set(Tables.ACTOR_DEFINITION.UPDATED_AT, timestamp)
            .set(Tables.ACTOR_DEFINITION.MAX_SECONDS_BETWEEN_MESSAGES,
                standardSourceDefinition.getMaxSecondsBetweenMessages() == null ? null
                    : standardSourceDefinition.getMaxSecondsBetweenMessages().intValue())
            .execute();
      }
    });
  }

  static void writeStandardDestinationDefinition(final List<StandardDestinationDefinition> configs, final DSLContext ctx) {
    final OffsetDateTime timestamp = OffsetDateTime.now();
    configs.forEach((standardDestinationDefinition) -> {
      final boolean isExistingConfig = ctx.fetchExists(DSL.select()
          .from(Tables.ACTOR_DEFINITION)
          .where(Tables.ACTOR_DEFINITION.ID.eq(standardDestinationDefinition.getDestinationDefinitionId())));

      if (isExistingConfig) {
        ctx.update(Tables.ACTOR_DEFINITION)
            .set(Tables.ACTOR_DEFINITION.ID, standardDestinationDefinition.getDestinationDefinitionId())
            .set(Tables.ACTOR_DEFINITION.NAME, standardDestinationDefinition.getName())
            .set(Tables.ACTOR_DEFINITION.ICON, standardDestinationDefinition.getIcon())
            .set(Tables.ACTOR_DEFINITION.ACTOR_TYPE, ActorType.destination)
            .set(Tables.ACTOR_DEFINITION.TOMBSTONE, standardDestinationDefinition.getTombstone())
            .set(Tables.ACTOR_DEFINITION.PUBLIC, standardDestinationDefinition.getPublic())
            .set(Tables.ACTOR_DEFINITION.CUSTOM, standardDestinationDefinition.getCustom())
            .set(Tables.ACTOR_DEFINITION.RESOURCE_REQUIREMENTS,
                standardDestinationDefinition.getResourceRequirements() == null ? null
                    : JSONB.valueOf(Jsons.serialize(standardDestinationDefinition.getResourceRequirements())))
            .set(Tables.ACTOR_DEFINITION.UPDATED_AT, timestamp)
            .where(Tables.ACTOR_DEFINITION.ID.eq(standardDestinationDefinition.getDestinationDefinitionId()))
            .execute();

      } else {
        ctx.insertInto(Tables.ACTOR_DEFINITION)
            .set(Tables.ACTOR_DEFINITION.ID, standardDestinationDefinition.getDestinationDefinitionId())
            .set(Tables.ACTOR_DEFINITION.NAME, standardDestinationDefinition.getName())
            .set(Tables.ACTOR_DEFINITION.ICON, standardDestinationDefinition.getIcon())
            .set(Tables.ACTOR_DEFINITION.ACTOR_TYPE, ActorType.destination)
            .set(Tables.ACTOR_DEFINITION.TOMBSTONE,
                standardDestinationDefinition.getTombstone() != null && standardDestinationDefinition.getTombstone())
            .set(Tables.ACTOR_DEFINITION.PUBLIC, standardDestinationDefinition.getPublic())
            .set(Tables.ACTOR_DEFINITION.CUSTOM, standardDestinationDefinition.getCustom())
            .set(Tables.ACTOR_DEFINITION.CREATED_AT, timestamp)
            .set(Tables.ACTOR_DEFINITION.UPDATED_AT, timestamp)
            .execute();
      }
    });
  }

}
