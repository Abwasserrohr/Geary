package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType


public fun family(init: FamilyBuilder.() -> Unit): Family = FamilyBuilder().apply(init).build()

public class FamilyBuilder {
    public var match: MutableList<GearyComponentId> = mutableListOf()
    public var andNot: MutableList<GearyComponentId> = mutableListOf()


    public fun match(vararg accessors: GearyComponentId) {
        match = accessors.toMutableList()
    }

    public fun andNot(vararg accessors: GearyComponentId) {
        andNot = accessors.toMutableList()
    }

    public fun build(): Family {
        return Family(match, andNot)
    }
}

public class Family(
    public val match: List<GearyComponentId> = listOf(),
    public val andNot: List<GearyComponentId> = listOf(),
) {
    public val type: GearyType = match.sorted()

    public operator fun contains(type: GearyType): Boolean =
        type.containsAll(match) && andNot.none { type.contains(it) }
}
