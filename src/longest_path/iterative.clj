(ns longest-path.iterative)

(defmacro max2 [a k]
  `(loop [m# 0 s# 0 idx# 0 i# 0]
     (if (= i# ~k)
       [m# s# idx#]
       (let [ai# (nth ~a i#)]
         (if (> ai# m#)
           (recur ai# m# i# (inc i#))
           (recur m# (max ai# s#) idx# (inc i#)))))))

(defn max3 [a k]
  (loop [m 0 s 0 t 0 idx 0 idy 0 i 0]
    (if (= i k)
      [m s t idx idy]
      (let [ai (nth a i)]
        (cond
          (>= ai m s) (recur ai m s i idx (inc i))
          (>= m ai s) (recur m ai s idx i (inc i))
          :else (recur m s (max ai t) idx idy (inc i)))))))

(defmacro max2DistinctFolios [a b k]
  `(let [[ma# sa# ia#] (max2 ~a ~k)
         [mb# sb# ib#] (max2 ~b ~k)]
     (if (not= ia# ib#)
       (+ ma# mb#)
       (max (+ ma# sb#)
            (+ sa# mb#)))))

(defn max3DistinctFolios [a b c k]
  (let [[ma sa ta ia ja] (max3 a k)
        [mb sb tb ib jb] (max3 b k)
        [mc sc tc ic jc] (max3 c k)
        ab-blocker (if (= ia ib) jb ib)
        ac-blocker (if (= ia ic) jc ic)
        ba-blocker (if (= ib ia) ja ia)
        bc-blocker (if (= ib ic) jc ic)
        ca-blocker (if (= ic ia) ja ia)
        cb-blocker (if (= ic ib) jb ib)]

    (max (+ ma                                              ;a b c
            (if (= ia ib) sb mb)
            (condp = ic
              ia (if (= jc ab-blocker) tc sc)
              ab-blocker (if (= jc ia) tc sc)
              ic mc))
         (+ ma                                              ;a c b
            (if (= ia ic) sc mc)
            (condp = ib
              ia (if (= jb ac-blocker) tb sb)
              ac-blocker (if (= jb ia) tb sb)
              ib mb))
         (+ mb                                              ;b c a
            (if (= ic ib) sc mc)
            (condp = ia
              ib (if (= ja bc-blocker) ta sa)
              bc-blocker (if (= ja ib) ta sa)
              ia ma))
         (+ mb                                              ;b a c
            (if (= ia ib) sa ma)
            (condp = ic
              ib (if (= jc ba-blocker) tc sc)
              ba-blocker (if (= jc ib) tc sc)
              ic mc))
         (+ mc                                              ;c a b
            (if (= ic ia) sa ma)
            (condp = ib
              ic (if (= jb ca-blocker) tb sb)
              ca-blocker (if (= jb ic) tb sb)
              ib mb))
         (+ mc                                              ;c b a
            (if (= ic ib) sb mb)
            (condp = ia
              ic (if (= ja cb-blocker) ta sa)
              cb-blocker (if (= ja ic) ta sa)
              ia ma)))))

(defn coe-impl [labels]
  (let [k (count labels)]
    (if (= k 1)
      (first labels)
      (let [[a1 a2 a3 a4 a5 a6 a7] (apply map vector labels)
            l7 (max (apply max a7)
                    (max2DistinctFolios a4 a6 k))
            l2 (apply max a2)
            l4 (apply max a4)
            l6 (apply max a6)
            l3 (max (apply max a3)
                    (max2DistinctFolios a2 a6 k))
            l5 (max (apply max a5)
                    (max2DistinctFolios a2 a4 k))
            l1 (max (apply max a1)
                    l2, l3, l4, l5, l6
                    (max2DistinctFolios a3 a4 k)
                    (max2DistinctFolios a5 a6 k)
                    (max2DistinctFolios a2 a7 k)
                    (max2DistinctFolios a4 a4 k)
                    (max2DistinctFolios a6 a6 k)
                    (if (>= k 3)
                      (max3DistinctFolios a2 a4 a6 k)
                      0))]
        ;(println "coe" (apply str labels))
        [l1 l2 l3 l4 l5 l6 l7]))))

(def combine-on-edge
  (let [mem (atom {})]
    (fn [args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (coe-impl args)]
          (swap! mem assoc args ret)
          ret)))))



(defn cofp-impl [[a1 a2 a3 a4 a5 a6 a7]
                 [b1 b2 b3 b4 b5 b6 b7]]
  ;(println "cof2" [a1 a2 a3 a4 a5 a6 a7] [b1 b2 b3 b4 b5 b6 b7])
  (let [l2 (+ a2 b2)
        l3 (max (inc b6)
                (+ a2 b3)
                (inc b2)
                (inc b5)
                (+ 1 b2 a6))
        l4 (max a3 a4 (+ a2 b4))
        l5 (max (inc a4)
                (+ b2 a5)
                (inc a2)
                (inc a3)
                (+ 1 a2 b4))
        l6 (max b5 b6 (+ b2 a6))
        l7 (max (+ a4 b6)
                (+ a4 b5)
                (+ a4 b2)
                (+ a2 b6)
                (+ a3 b6)
                (+ a2 b7)
                (+ a7 b2))
        l1 (max l2 l3 l4 l5 l6 a1 b1 (inc l7) (+ (max a5 a6)
                                                 (max b3 b4)))]
    [l1 l2 l3 l4 l5 l6 l7]))

(def combine-on-face-proper
  (let [mem (atom {})]
    (fn [a b]
      ;(println [a b])
      (if-let [e (find @mem [a b])]
        (val e)
        (let [ret (cofp-impl a b)]
          (swap! mem assoc [a b] ret)
          ret)))))


(defn cof-impl-left [[a1 a2 a3 a4 a5 a6 a7]]
  (let [l3 (+ 2 a6)
        l5 (inc (max a2 a3 a4 a5))
        l7 (max (inc a4) a2 a3 (inc a7))
        l1 (max l3 l5 a1 (inc l7))]
    [l1 (inc a2) l3 (max a2 a3 a4 ) l5 (inc a6) l7]))

(def combine-on-face-left
  (let [mem (atom {})]
    (fn [label]
      (if (<= (first label) 10)
        (if-let [e (find @mem [label])]
          (val e)
          (let [ret (cof-impl-left label)]
            (swap! mem assoc [label] ret)
            ret)))
      (cof-impl-left label))))

(defmacro reverse-label [label]
  `(let [[a1# a2# a3# a4# a5# a6# a7#] ~label]
     [a1# a2# a5# a6# a3# a4# a7#]))

(defn compute-label-edge [a b EdgeLabels key]
  (if-let [labels (get EdgeLabels key)]
    (let [label (combine-on-edge (persistent! labels))]
      (if (< b a) (reverse-label label) label))
    false))

(defmacro combine-on-face-right [x]
  `(reverse-label (combine-on-face-left (reverse-label ~x))))

(defn compute-degrees [tree]
  (loop [result (transient (clojure.data.int-map/int-map))
         d-seq tree
         deg2 (list)]
    (if (empty? d-seq)
      [result deg2]
      (let [[vertex neighbours] (first d-seq)
            degree (count neighbours)]
        (recur (assoc! result vertex degree)
               (rest d-seq)
               (if (= degree 2) (conj deg2 vertex) deg2))))))

(defn longest-path-iterative [tree]
  (loop [data tree
         [degrees unprocessed] (compute-degrees tree)
         EdgeLabels (transient {})]
    (let [w (first unprocessed)
          rst (pop unprocessed)
          edge (get data w)
          u (apply min edge)
          v (apply max edge)
          degU (dec (get degrees u))
          degV (dec (get degrees v))
          addU (= 2 degU)
          addV (= 2 degV)
          key1 (if (< u w) [u w] [w u])
          key2 (if (< w v) [w v] [v w])
          label1 (compute-label-edge u w EdgeLabels key1)
          label2 (compute-label-edge w v EdgeLabels key2)
          newEdgeLabels (cond (and label1 label2) (dissoc! EdgeLabels key1 key2)
                              label1 (dissoc! EdgeLabels key1)
                              label2 (dissoc! EdgeLabels key2)
                              :else EdgeLabels)
          label (cond (and label1 label2) (combine-on-face-proper label1 label2)
                      label1 (combine-on-face-left label1)
                      label2 (combine-on-face-right label2)
                      :else [2 2 2 1 2 1 1])]
      (if (= 1 degU degV)
        (first label)
        (recur (assoc! (dissoc! data w)
                       u (disj (data u) w)
                       v (disj (data v) w))
               [(assoc! (dissoc! degrees w)
                        u degU
                        v degV)
                (cond (and addU addV) (conj rst u v)
                      addU (conj rst u)
                      addV (conj rst v)
                      :else rst)]
               (assoc! newEdgeLabels [u v] (conj! (get EdgeLabels [u v] (transient [])) label)))))))