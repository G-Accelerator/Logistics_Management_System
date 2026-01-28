import { defineStore } from "pinia";
import { ref } from "vue";
import type { RouteLocationNormalizedLoaded } from "vue-router";
import type { TagView } from "./types";

export type { TagView };

export const useTagsViewStore = defineStore("tagsView", () => {
  const visitedViews = ref<TagView[]>([]);
  const cachedViews = ref<string[]>([]);

  // 添加访问过的视图
  const addView = (route: RouteLocationNormalizedLoaded) => {
    addVisitedView(route);
    addCachedView(route);
  };

  // 添加到访问列表
  const addVisitedView = (route: RouteLocationNormalizedLoaded) => {
    // 使用 name 判断是否已存在，避免重复
    if (visitedViews.value.some((v) => v.name === route.name)) return;

    visitedViews.value.push({
      name: route.name as string,
      path: route.path,
      title: route.meta?.title as string,
      query: route.query,
      params: route.params,
      affix: route.meta?.affix as boolean,
    });
  };

  // 添加到缓存列表
  const addCachedView = (route: RouteLocationNormalizedLoaded) => {
    if (route.meta?.noCache) return;
    if (cachedViews.value.includes(route.name as string)) return;

    cachedViews.value.push(route.name as string);
  };

  // 删除视图
  const delView = (view: TagView) => {
    return new Promise<void>((resolve) => {
      delVisitedView(view);
      delCachedView(view);
      resolve();
    });
  };

  // 从访问列表删除
  const delVisitedView = (view: TagView) => {
    for (const [i, v] of visitedViews.value.entries()) {
      if (v.name === view.name) {
        visitedViews.value.splice(i, 1);
        break;
      }
    }
  };

  // 从缓存列表删除
  const delCachedView = (view: TagView) => {
    const index = cachedViews.value.indexOf(view.name);
    if (index > -1) {
      cachedViews.value.splice(index, 1);
    }
  };

  // 删除其他视图
  const delOthersViews = (view: TagView) => {
    return new Promise<void>((resolve) => {
      delOthersVisitedViews(view);
      delOthersCachedViews(view);
      resolve();
    });
  };

  // 删除其他访问视图
  const delOthersVisitedViews = (view: TagView) => {
    visitedViews.value = visitedViews.value.filter((v) => {
      return v.affix || v.path === view.path;
    });
  };

  // 删除其他缓存视图
  const delOthersCachedViews = (view: TagView) => {
    const index = cachedViews.value.indexOf(view.name);
    if (index > -1) {
      cachedViews.value = cachedViews.value.slice(index, index + 1);
    } else {
      cachedViews.value = [];
    }
  };

  // 删除所有视图
  const delAllViews = () => {
    return new Promise<void>((resolve) => {
      delAllVisitedViews();
      delAllCachedViews();
      resolve();
    });
  };

  // 删除所有访问视图
  const delAllVisitedViews = () => {
    visitedViews.value = visitedViews.value.filter((tag) => tag.affix);
  };

  // 删除所有缓存视图
  const delAllCachedViews = () => {
    cachedViews.value = [];
  };

  // 更新访问视图
  const updateVisitedView = (view: TagView) => {
    for (let v of visitedViews.value) {
      if (v.name === view.name) {
        v = Object.assign(v, view);
        break;
      }
    }
  };

  return {
    visitedViews,
    cachedViews,
    addView,
    delView,
    delCachedView,
    delOthersViews,
    delAllViews,
    updateVisitedView,
  };
});
