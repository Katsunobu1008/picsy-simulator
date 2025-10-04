<!-- src/components/StatePanel.vue -->
<template>
  <h2 style="margin-top:0;">Current State</h2>

  <!-- メタ情報 -->
  <div class="row" style="justify-content: space-between;">
    <div>Members: <strong>{{ N }}</strong></div>
    <div v-if="N">Σc = <strong>{{ fmt(sumC) }}</strong></div>
  </div>

  <!-- 指定形式の表 -->
  <table class="table" v-if="N">
    <thead>
      <tr>
        <th style="min-width:80px;">c</th>           <!-- ← c_i -->
        <th style="min-width:100px;">Eii*c</th>      <!-- ← E_{ii} * c_i -->
        <th style="min-width:60px;">i/j</th>         <!-- ← 行番号（1始まり） -->
        <th v-for="j in N" :key="'c'+j">{{ j }}</th> <!-- ← 列番号（1始まり） -->
      </tr>
    </thead>
    <tbody>
      <tr v-for="i in N" :key="'r'+i">
        <td>{{ fmt(c[i-1]) }}</td>                                   <!-- c_i -->
        <td>{{ fmt(power[i-1]) }}</td>                               <!-- E_ii * c_i -->
        <th style="text-align:center;">{{ i }}</th>                   <!-- i/j -->
        <td v-for="j in N" :key="'e'+i+'-'+j">
          {{ fmt(E[i-1][j-1]) }}                                      <!-- E_{ij} -->
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
import { computed } from 'vue';
import { state, fmt } from '../store/usePicsy.js';

// N, E, c, power を取り出す
const N = computed(()=> state.matrix?.length || 0);
const E = computed(()=> state.matrix || []);
const c = computed(()=> state.contributions || []);
const power = computed(()=> state.power || []);

// Σc を計算（安全に Number 化）
const sumC = computed(()=> {
  const arr = c.value || [];
  return arr.reduce((acc, v)=> acc + (Number.isFinite(v) ? v : 0), 0);
});
</script>
