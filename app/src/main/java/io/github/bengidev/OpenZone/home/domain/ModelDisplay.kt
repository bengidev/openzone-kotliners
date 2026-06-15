package io.github.bengidev.openzone.home.domain

/** Humanizes a wire model id when the catalog no longer lists it. Mirrors iOS `HomeModelCatalog.displayTitle`. */
internal fun displayTitleForModelId(modelId: String): String {
    val leaf = modelId.substringAfterLast('/', modelId)
    return leaf.removeSuffix(":free").replace('-', ' ').replace('_', ' ')
}
