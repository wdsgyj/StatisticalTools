package com.baidu.statools.gen

/**
 * Created by clark on 14-12-3.
 */
class LayoutTranslation implements Translation<LayoutFile, List<File>> {
    private AddFlagTranslation addFlagTranslation
    private ModifyTagNameTranslation modifyTagNameTranslation

    LayoutTranslation(Set<Integer> flags, Map<String, String> table) {
        addFlagTranslation = new AddFlagTranslation(flags)
        modifyTagNameTranslation = new ModifyTagNameTranslation(table)
    }

    @Override
    List<File> translate(LayoutFile src) {
        List<File> files = []
        files << addFlagTranslation.translate(src)
        files << modifyTagNameTranslation.translate(src)
        return files
    }
}
