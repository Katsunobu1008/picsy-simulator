<!-- src/App.vue -->
<template>
  <header class="panel" style="margin:16px;">
    <div class="row" style="justify-content:space-between; align-items:center;">
      <h1 style="margin:0; font-size:18px;">PICSY Dashboard (Vue)</h1>
      <div>
        <span v-if="error" class="msg err" role="alert">{{ error }}</span>
        <span v-else-if="loading" class="msg ok">Loading...</span>
      </div>
    </div>
  </header>

  <!  -- 2カラム：左=Initialize + 操作パネル、右=E & c & Power の表 -->
  <main class="grid" style="grid-template-columns: 420px 1fr; margin:16px; column-gap:16px;">
    <section class="panel" style="display:flex; flex-direction:column; gap:16px; overflow:auto;">
      <InitializeForm />
      <ControlPanel />
    </section>
    <section class="panel" style="overflow:auto;">
      <StatePanel />
    </section>
  </main>
</template>

<script setup>
import { onMounted, computed } from 'vue';
import InitializeForm from './components/InitializeForm.vue';
import StatePanel from './components/StatePanel.vue';
import ControlPanel from './components/ControlPanel.vue';
import { state, fetchState } from './store/usePicsy.js';

const loading = computed(()=> state.loading);
const error   = computed(()=> state.error);

onMounted(async () => {
  try { await fetchState(); } catch {}
});
</script>
