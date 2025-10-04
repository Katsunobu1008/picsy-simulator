<!-- src/components/InitializeForm.vue -->
<template>
  <h2 style="margin-top:0;">Initialize</h2>

  <!-- Matrix（必須）：行単位CSV（N行×N列）、正方ならOK、正規化はサーバ -->
  <div class="grid">
    <label for="matrix">Evaluation Matrix E (CSV rows, N lines with N comma-separated numbers)</label>
    <textarea
      id="matrix"
      v-model="matrixText"
      rows="10"
      aria-label="Evaluation matrix CSV"
      @keydown.ctrl.enter.prevent="onSubmit"    <!-- Ctrl+Enter でも送信 -->
    ></textarea>
  </div>

  <div class="row" style="margin-top:12px;">
    <button class="btn primary" :disabled="loading" @click="onSubmit">Initialize</button>
    <button class="btn" :disabled="loading" @click="onClear">Clear</button>
  </div>

  <p v-if="message" class="msg ok" aria-live="polite">{{ message }}</p>
  <p v-if="error" class="msg err" role="alert">{{ error }}</p>
</template>

<script setup>
// ---- 目的：CSV行のテキストを行列(double[][])に変換して /api/initialize に送る ----
import { ref, computed } from 'vue';
import { initializeCommunity, state } from '../store/usePicsy.js';

// 入力（1テキストエリアのみ）
const matrixText = ref('');

// 状態
const loading = computed(()=> state.loading);
const error   = computed(()=> state.error);
const message = ref('');

// CSV文字列 -> 2D number[]
function parseMatrix(text) {
  const rows = text.trim().split(/\r?\n/).filter(Boolean);          // ← 空行を除外
  const matrix = rows.map(row => row.split(',').map(s => Number(s.trim()))); // ← カンマで分割→数値
  const n = matrix.length;
  if (!n || matrix.some(r => r.length !== n)) {                      // ← 正方チェック
    throw new Error('Matrix must be square: N lines with N comma-separated numbers.');
  }
  for (const r of matrix) for (const v of r) {
    if (!Number.isFinite(v)) throw new Error('Matrix contains a non-numeric value.');
  }
  return matrix;
}

async function onSubmit() {
  message.value = '';
  try {
    const matrix = parseMatrix(matrixText.value);          // ← CSV を 2D 数値配列に
    await initializeCommunity(matrix);                     // ← names不要。{names:null, matrix} を送信
    message.value = 'Initialized successfully.';
  } catch (e) {
    console.error(e);
  }
}

function onClear() {
  matrixText.value = '';
  message.value = '';
}
</script>
