package com.automattic.simplenote.utils.markdown

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.SimpleInlineTagProvider
import org.intellij.markdown.html.URI
import org.intellij.markdown.parser.LinkMap

class SimplenoteMarkdownFlavorDescriptor : GFMFlavourDescriptor() {
    override fun createHtmlGeneratingProviders(linkMap: LinkMap,
                                               baseURI: URI?): Map<IElementType, GeneratingProvider> {
        return super.createHtmlGeneratingProviders(linkMap, baseURI) + hashMapOf(
            GFMElementTypes.STRIKETHROUGH to object : SimpleInlineTagProvider("del", 2, -2) {
            },
        )
    }
}
