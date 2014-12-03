package com.baidu.statools.gen

import org.apache.commons.io.IOUtils
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter

/**
 * Created by clark on 14-12-3.
 */
class ModifyTagNameTranslation implements Translation<LayoutFile, File> {
    Map<String, String> nameTable = new HashMap<>()

    ModifyTagNameTranslation(Map<String, String> nameTable) {
        this.nameTable = nameTable
    }

    @Override
    File translate(LayoutFile src) {
        File outputFile = new File(src.outputDir, src.source.name)
        SAXReader reader = new SAXReader()
        Document document = reader.read(src.source)
        LinkedList<Element> elements = new LinkedList<>()
        elements.add(document.rootElement)
        while (elements.size() > 0) {
            Element e = elements.removeFirst()
            if (e) {
                String newName = nameTable[e.name]
                if (newName) {
                    e.setName(newName)
                }

                List<Element> children = e.elements()
                if (children) {
                    // 按深度遍历，修改 tag 名字
                    elements.addAll(0, children)
                }
            }
        }

        Writer fileWriter = new FileWriter(outputFile)
        XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createCompactFormat())
        try {
            writer.write(document)
        } finally {
            IOUtils.closeQuietly(fileWriter)
        }
        return outputFile
    }
}
