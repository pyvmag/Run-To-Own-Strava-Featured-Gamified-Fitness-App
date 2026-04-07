(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, global.rewind = factory());
}(this, (function () { 'use strict';

  function rewind(gj, outer) {
      var type = gj && gj.type,
          i = 0;
      if (type === 'FeatureCollection') {
          for (; i < gj.features.length; i++) rewind(gj.features[i], outer);
      } else if (type === 'Feature') {
          rewind(gj.geometry, outer);
      } else if (type === 'Polygon') {
          rewindRings(gj.coordinates, outer);
      } else if (type === 'MultiPolygon') {
          for (; i < gj.coordinates.length; i++) rewindRings(gj.coordinates[i], outer);
      }
      return gj;
  }

  function rewindRings(rings, outer) {
      if (rings.length === 0) return;
      rewindRing(rings[0], outer);
      for (var i = 1; i < rings.length; i++) {
          rewindRing(rings[i], !outer);
      }
  }

  function rewindRing(ring, dir) {
      var area = 0,
          i = 0,
          len = ring.length,
          j = len - 1;
      for (; i < len; j = i++) {
          area += ring[i][0] * ring[j][1] - ring[j][0] * ring[i][1];
      }
      if (dir === (area > 0)) ring.reverse();
  }

  return rewind;

})));