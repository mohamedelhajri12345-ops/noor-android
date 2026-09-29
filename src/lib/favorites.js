// نظام المفضلة — حفظ السور والقصص للوصول السريع
// يستخدم localStorage للعمل أوفلاين

const FAV_KEY = 'nur_favorites';

export const getFavorites = () => {
  try { return JSON.parse(localStorage.getItem(FAV_KEY) || '[]'); } catch { return []; }
};

const saveFavorites = (favs) => {
  localStorage.setItem(FAV_KEY, JSON.stringify(favs));
  window.dispatchEvent(new CustomEvent('favorites-changed'));
};

export const isFavorite = (type, id) => {
  const favs = getFavorites();
  const strId = String(id);
  return favs.some(f => f.type === type && String(f.id) === strId);
};

export const addFavorite = (item) => {
  const favs = getFavorites();
  if (!favs.some(f => f.type === item.type && String(f.id) === String(item.id))) {
    favs.push({ ...item, id: String(item.id), savedAt: Date.now() });
    saveFavorites(favs);
  }
};

export const removeFavorite = (type, id) => {
  const strId = String(id);
  const favs = getFavorites().filter(f => !(f.type === type && String(f.id) === strId));
  saveFavorites(favs);
};

// Returns true if added, false if removed
export const toggleFavorite = (item) => {
  if (isFavorite(item.type, item.id)) {
    removeFavorite(item.type, item.id);
    return false;
  }
  addFavorite(item);
  return true;
};

export const getFavoritesByType = (type) => {
  return getFavorites().filter(f => f.type === type);
};
