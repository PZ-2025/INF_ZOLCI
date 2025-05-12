<template>
  <div class="raport-view">
    <h1>{{ raport.name }}</h1>
    <pre class="raport-content">{{ raport.content }}</pre>
    <button @click="downloadRaport" class="btn-download">Pobierz</button>
  </div>
</template>

<script>
export default {
  props: {
    raport: {
      type: Object,
      required: true,
    },
  },
  methods: {
    downloadRaport() {
      const blob = new Blob([this.raport.content], { type: "text/plain" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `${this.raport.name}.txt`;
      link.click();
      URL.revokeObjectURL(link.href);
    },
  },
};
</script>

<style scoped>
.raport-view {
  padding: 20px;
  font-family: Arial, sans-serif;
}

.raport-content {
  margin-top: 20px;
  padding: 10px;
  border: 1px solid #ccc;
  background-color: #f9f9f9;
  white-space: pre-wrap;
}

.btn-download {
  margin-top: 10px;
  padding: 5px 10px;
  cursor: pointer;
}
</style>
