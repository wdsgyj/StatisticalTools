package com.baidu.statools.scanner

import com.baidu.statools.gen.TemplateCodes
import org.dom4j.Attribute
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
/**
 * Created on 2014/12/4.
 *
 * @author clark
 */
class LayoutFileScanner {
    private List<File> layoutFiles = []

    LayoutFileScanner(List<File> files) {
        layoutFiles.addAll(files)
    }

    private Set<Integer> scannerFlags() {
        Set<Integer> rs = new HashSet<>()
        SAXReader reader = new SAXReader()
        layoutFiles.each { file ->
            Document document = reader.read(file)
            Element root = document.rootElement
            Attribute attr = root.attribute(TemplateCodes.baiduMapFlag)
            if (attr) {
                try {
                    rs.add(attr.value as int)
                } catch (Exception ex) {
                }
            }
            root.elementIterator().each { Element e ->
                attr = e.attribute(TemplateCodes.baiduMapFlag)
                if (attr) {
                    try {
                        rs.add(attr.value as int)
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return rs
    }
}
