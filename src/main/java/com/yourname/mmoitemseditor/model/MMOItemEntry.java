package com.yourname.mmoitemseditor.model;

import java.io.File;

/**
 * 表示一个MMOItems物品条目，用于在TreeView中显示。
 * 包含物品ID和其所属的YML文件。
 */
public class MMOItemEntry {
    private String itemId;
    private File parentFile; // 物品所属的YML文件

    public MMOItemEntry(String itemId, File parentFile) {
        this.itemId = itemId;
        this.parentFile = parentFile;
    }

    public String getItemId() {
        return itemId;
    }

    public File getParentFile() {
        return parentFile;
    }

    @Override
    public String toString() {
        // TreeView将调用此方法来显示节点文本
        return itemId;
    }

    // 可以添加equals和hashCode方法，如果需要基于itemId和parentFile进行比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MMOItemEntry that = (MMOItemEntry) o;
        return itemId.equals(that.itemId) && parentFile.equals(that.parentFile);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(itemId, parentFile);
    }
}