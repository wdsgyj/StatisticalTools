package com.baidu.statools.gen

import org.apache.commons.io.IOUtils
import org.dom4j.Attribute
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter

import java.util.concurrent.atomic.AtomicLong
/**
 * Created by clark on 14-12-3.
 */
class AddFlagTranslation implements Translation<LayoutFile, File> {
    Set<Integer> alreadyHave = new HashSet<>()
    final AtomicLong sAutoFlags = new AtomicLong(0)

    AddFlagTranslation (Set<Integer> flags) {
        alreadyHave << flags
    }

    @Override
    File translate(LayoutFile src) {
        File outputFile = new File(src.outputDir, src.source.name)
        SAXReader reader = new SAXReader()
        Document document = reader.read(src.source)
        int flag
        // 如果该 flag 已经存在则继续自增
        while (alreadyHave.contains(flag = sAutoFlags.addAndGet(1)));
        LinkedList<Element> elements = new LinkedList<>()
        elements.add(document.rootElement)
        while (elements.size() > 0) {
            Element e = elements.removeFirst()
            if (e) {
                Attribute attribute = e.attribute(TemplateCodes.baiduMapFlag)
                // 如果不存在 flag 属性则添加一个
                if (!attribute) {
                    e.addAttribute(TemplateCodes.baiduMapFlag, "$flag")
                }
                List<Element> children = e.elements()
                if (children) {
                    // 按深度遍历，添加 flag 属性
                    elements.addAll(0, children)
                }
            }
        }

        Writer fileWriter = new FileWriter(outputFile)
        XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint())
        try {
            writer.write(document)
        } finally {
            IOUtils.closeQuietly(fileWriter)
        }
        return outputFile
    }
}
