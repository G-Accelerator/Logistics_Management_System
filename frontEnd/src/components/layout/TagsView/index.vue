<template>
  <div class="tags-view-container">
    <el-scrollbar class="tags-view-wrapper">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :to="{ path: tag.path, query: tag.query }"
        :class="isActive(tag) ? 'active' : ''"
        class="tags-view-item"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        {{ tag.title }}
        <el-icon
          v-if="!isAffix(tag)"
          class="close-icon"
          @click.prevent.stop="closeSelectedTag(tag)"
        >
          <Close />
        </el-icon>
      </router-link>
    </el-scrollbar>

    <ul
      v-show="visible"
      :style="{ left: left + 'px', top: top + 'px' }"
      class="contextmenu"
    >
      <li @click="refreshSelectedTag(selectedTag)">
        <el-icon><Refresh /></el-icon>
        刷新
      </li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">
        <el-icon><Close /></el-icon>
        关闭
      </li>
      <li @click="closeOthersTags">
        <el-icon><CircleClose /></el-icon>
        关闭其他
      </li>
      <li @click="closeAllTags">
        <el-icon><CircleClose /></el-icon>
        关闭所有
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useTagsViewStore } from "../../../store/tagsView";
import type { TagView } from "./types";
import { Close, Refresh, CircleClose } from "@element-plus/icons-vue";

export type { TagView } from "./types";

const route = useRoute();
const router = useRouter();
const tagsViewStore = useTagsViewStore();

const visible = ref(false);
const top = ref(0);
const left = ref(0);
const selectedTag = ref<TagView>({} as TagView);

const visitedViews = computed(() => tagsViewStore.visitedViews);

const isActive = (tag: TagView) => tag.name === route.name;
const isAffix = (tag: TagView) => tag.affix;

const addTags = () => {
  if (route.name && route.meta?.title) {
    tagsViewStore.addView(route);
  }
};

const closeSelectedTag = (view: TagView) => {
  tagsViewStore.delView(view).then(() => {
    if (isActive(view)) toLastView();
  });
};

const closeOthersTags = () => {
  tagsViewStore.delOthersViews(selectedTag.value).then(() => {
    if (!isActive(selectedTag.value)) router.push(selectedTag.value.path);
  });
  closeMenu();
};

const closeAllTags = () => {
  tagsViewStore.delAllViews().then(() => toLastView());
  closeMenu();
};

const refreshSelectedTag = (view: TagView) => {
  tagsViewStore.delCachedView(view);
  nextTick(() => {
    router
      .replace({ path: view.path, query: view.query })
      .then(() => router.go(0));
  });
  closeMenu();
};

const toLastView = () => {
  const latestView = visitedViews.value.slice(-1)[0];
  latestView ? router.push(latestView.path) : router.push("/");
};

const openMenu = (tag: TagView, e: MouseEvent) => {
  const menuMinWidth = 105;
  const offsetLeft = e.clientX;
  const offsetWidth = document.documentElement.clientWidth;
  const maxLeft = offsetWidth - menuMinWidth;
  left.value = offsetLeft > maxLeft ? maxLeft : offsetLeft;
  top.value = e.clientY;
  visible.value = true;
  selectedTag.value = tag;
};

const closeMenu = () => {
  visible.value = false;
};

watch(
  () => route.path,
  () => addTags(),
  { immediate: true },
);
watch(visible, (value) => {
  value
    ? document.body.addEventListener("click", closeMenu)
    : document.body.removeEventListener("click", closeMenu);
});
</script>

<style scoped>
.tags-view-container {
  height: 40px;
  width: 100%;
  background: var(--tags-bg);
  border-bottom: 1px solid var(--tags-border);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.08);
}
.tags-view-wrapper {
  height: 100%;
}
.tags-view-wrapper :deep(.el-scrollbar__wrap) {
  height: 49px;
}
.tags-view-item {
  display: inline-block;
  position: relative;
  cursor: pointer;
  height: 32px;
  line-height: 32px;
  border: 1px solid var(--tags-item-border);
  color: var(--text-secondary);
  background: var(--tags-item-bg);
  padding: 0 12px;
  font-size: 12px;
  margin-left: 5px;
  margin-top: 4px;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s;
}
.tags-view-item:first-of-type {
  margin-left: 15px;
}
.tags-view-item:hover {
  color: var(--primary-color);
  border-color: var(--primary-light);
  background: var(--border-color-light);
}
.tags-view-item.active {
  background-color: var(--tags-item-active-bg);
  color: var(--tags-item-active-color);
  border-color: var(--tags-item-active-bg);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}
.tags-view-item.active::before {
  content: "";
  background: #fff;
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
  margin-right: 4px;
}
.close-icon {
  width: 14px;
  height: 14px;
  margin-left: 4px;
  border-radius: 50%;
  transition: all 0.3s;
}
.close-icon:hover {
  background-color: rgba(0, 0, 0, 0.15);
  color: var(--text-primary);
}
.tags-view-item.active .close-icon:hover {
  background-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}
.contextmenu {
  margin: 0;
  background: var(--card-bg);
  z-index: 3000;
  position: fixed;
  list-style-type: none;
  padding: 5px 0;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 400;
  color: var(--text-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-color);
}
.contextmenu li {
  margin: 0;
  padding: 7px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}
.contextmenu li:hover {
  background: var(--border-color-light);
  color: var(--primary-color);
}
</style>
