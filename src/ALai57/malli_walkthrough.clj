(ns ALai57.malli-walkthrough
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.transform :as mt])
  (:gen-class))


(comment
  ;; A Malli walkthrough.

  "One of the challenges of writing Clojure is understanding the data flowing
  through the program. Since idiomatic Clojure leans heavily on passing maps
  around, it's often not immediately clear *what* a function's inputs or outputs
  look like.  The community has developed several solutions to this problem,
  many of which rely on the idea of a data specification language
  (Plumatic/schema, spec, Malli).

  Since Malli is being used widely at my workplace, I wanted to do a deeper
  dive into how it works under the hood.

  Malli is a Clojure(script) library that provides a language and tools for
  defining the shape of data. Per Malli's documentation;
  '... data models should be first-class: they should drive the runtime value
  transformations, forms and processes.'

  There are 3 key concepts that I want to explore with a motivating example of
  specifying a key-value pair representing a customer ID and transforming the
  ID value (a UUID) to and from JSON:
  1) Data models
  2) Data transformations
  3) Data validation

  Let's start with the basic premise that we want to decode a JSON field as a
  Clojure UUID."

  (m/decode [:map [:customer-id :uuid]]
            {:customer-id "facf2030-2dd0-4155-b22b-6a49f7a38e9a"}
            (mt/json-transformer))
  ;; => {:customer-id #uuid "facf2030-2dd0-4155-b22b-6a49f7a38e9a"}


  ;; The JSON transformer isn't directly connected to the Reigstry. It is fed
  ;; separately by a `-json-encoders` and  `-json-decoders`: Mappings between ?
  ;; and functions.
  (m/-into-transformer (mt/json-transformer))
  (m/-transformer-chain (mt/json-transformer))
  [{:name     :json
    :decoders {:=                 {:compile #function[malli.transform/-infer-child-decoder-compiler]}
               :double            #function[malli.transform/-number->double]
               :enum              {:compile #function[malli.transform/-infer-child-decoder-compiler]}
               :keyword           #function[malli.transform/-string->keyword]
               :map-of            {:compile #function[malli.transform/json-transformer/fn--15830]}
               :qualified-keyword #function[malli.transform/-string->keyword]
               :qualified-symbol  #function[malli.transform/-string->symbol]
               :set               #function[malli.transform/-sequential->set]
               :symbol            #function[malli.transform/-string->symbol]
               :uuid              #function[malli.transform/-string->uuid]
               double?            #function[malli.transform/-number->double]
               ident?             #function[malli.transform/-string->keyword]
               inst?              #function[malli.transform/-string->date]
               keyword?           #function[malli.transform/-string->keyword]
               qualified-ident?   #function[malli.transform/-string->keyword]
               qualified-keyword? #function[malli.transform/-string->keyword]
               qualified-symbol?  #function[malli.transform/-string->symbol]
               simple-ident?      #function[malli.transform/-string->keyword]
               simple-keyword?    #function[malli.transform/-string->keyword]
               simple-symbol?     #function[malli.transform/-string->symbol]
               symbol?            #function[malli.transform/-string->symbol]
               uuid?              #function[malli.transform/-string->uuid]}
    :encoders {:keyword           #function[malli.core/-keyword->string]
               :qualified-keyword #function[malli.core/-keyword->string]
               :qualified-symbol  #function[malli.transform/-any->string]
               :symbol            #function[malli.transform/-any->string]
               :uuid              #function[malli.transform/-any->string]
               inst?              #function[malli.transform/-date->string]
               keyword?           #function[malli.core/-keyword->string]
               qualified-keyword? #function[malli.core/-keyword->string]
               qualified-symbol?  #function[malli.transform/-any->string]
               ratio?             #function[malli.transform/-number->double]
               simple-keyword?    #function[malli.core/-keyword->string]
               simple-symbol?     #function[malli.transform/-any->string]
               symbol?            #function[malli.transform/-any->string]
               uuid?              #function[malli.transform/-any->string]}}]

  "INSERT IMAGE OF DEPENDENCY GRAPH HERE.
   This simple form is actually doing a lot under the hood.

  A. Create a decoder
    0. Create a `malli.core/Transformer` that knows how to decode JSON
    1. Create a `malli.core/Schema` from the `[:map [:customer-id :uuid]]` argument
      1a. Look up the schema in the global registry
    2. Using the `malli.core/Schema` from 1., use the `-transformer` method to look up
  "

  ;; TODO HERE
  ;; Here's a skeleton of what happens: We'll dive into this in separate parts
  ;; 1) Turn schema into Schema
  ;; 2) Look up the Transformer for the Schema
  (#'m/-lookup :uuid {})

  ;; By default, Malli stores schemas in a global registry
  m/default-registry


  ;; We can get the schemas from the registy
  (mr/schemas m/default-registry)
  (mr/schema m/default-registry :multi)

  ;; These schemas are loaded by default
  (merge (m/predicate-schemas)  ;; Predicate functions
         (m/class-schemas)      ;; just regular expression Patterns in java
         (m/comparator-schemas) ;; >, <, etc
         (m/type-schemas)       ;; :int, :nil, :etc
         (m/sequence-schemas)   ;; :+, :cat, etc
         (m/base-schemas)       ;; :and, :or, :map
         )



  (satisfies? mr/Registry m/default-registry)
  (mr/registry? m/default-registry)

  ;; There are different registry types:
  ;; fast-registry      : Uses a java.util.Hashmap
  ;; simple-registry    : Uses a clojure map
  ;; composite-registry : Multiple registries - combines several
  ;; mutable-registry   : Reference type that can be dereferenced
  ;; dynamic-registry   : Relies on a dynamic var in malli/registry
  ;; lazy-registry      :








  ;; When we invoke a `mt/transformer`, Malli creates two "convenience"
  ;; overrides for us that allow us to override the normal transformation logic.
  ;;
  ;; This is sort of like how clojure Protocols emit convenience functions when
  ;; you create them: `map->MyProtocol` and `->MyProtocol` if you know the
  ;; trick, it can be really useful.
  ;;
  ;; In Malli, transformers that are created via `mt/transfomer` assemble a
  ;; `chain` of interceptors with the convenience keys hidden/embedded in the
  ;; interceptor chain.
  ;;
  ;; Here's an example of the interceptor chain for the `mt/json-transformer`
  ;;
  ;; chain' from mt/transformer
  [{:decode {:transformers {:=                 {:compile #function[malli.transform/-infer-child-decoder-compiler]}
                            :double            #function[malli.transform/-number->double]
                            :enum              {:compile #function[malli.transform/-infer-child-decoder-compiler]}
                            :keyword           #function[malli.transform/-string->keyword]
                            :map-of            {:compile #function[malli.transform/json-transformer/fn--15830]}
                            :qualified-keyword #function[malli.transform/-string->keyword]
                            :qualified-symbol  #function[malli.transform/-string->symbol]
                            :set               #function[malli.transform/-sequential->set]
                            :symbol            #function[malli.transform/-string->symbol]
                            :uuid              #function[malli.transform/-string->uuid]
                            double?            #function[malli.transform/-number->double]
                            ident?             #function[malli.transform/-string->keyword]
                            inst?              #function[malli.transform/-string->date]
                            keyword?           #function[malli.transform/-string->keyword]
                            qualified-ident?   #function[malli.transform/-string->keyword]
                            qualified-keyword? #function[malli.transform/-string->keyword]
                            qualified-symbol?  #function[malli.transform/-string->symbol]
                            simple-ident?      #function[malli.transform/-string->keyword]
                            simple-keyword?    #function[malli.transform/-string->keyword]
                            simple-symbol?     #function[malli.transform/-string->symbol]
                            symbol?            #function[malli.transform/-string->symbol]
                            uuid?              #function[malli.transform/-string->uuid]}
             :default      nil
             ;; Notice how there are two `:keys` here: these allow us to
             ;; override the default encoding and decoding behavior using
             ;; annotations directly on the Malli schema.
             :keys         [[:decode :json] [:decode/json]]}
    :encode {:transformers {:keyword           #function[malli.core/-keyword->string]
                            :qualified-keyword #function[malli.core/-keyword->string]
                            :qualified-symbol  #function[malli.transform/-any->string]
                            :symbol            #function[malli.transform/-any->string]
                            :uuid              #function[malli.transform/-any->string]
                            inst?              #function[malli.transform/-date->string]
                            keyword?           #function[malli.core/-keyword->string]
                            qualified-keyword? #function[malli.core/-keyword->string]
                            qualified-symbol?  #function[malli.transform/-any->string]
                            ratio?             #function[malli.transform/-number->double]
                            simple-keyword?    #function[malli.core/-keyword->string]
                            simple-symbol?     #function[malli.transform/-any->string]
                            symbol?            #function[malli.transform/-any->string]
                            uuid?              #function[malli.transform/-any->string]}
             :default      ni
             l
             :keys         [[:encode :json] [:encode/json]]}}]


  ;; Here's an example where we override the default `uuid` decodeer using the
  ;; two Malli `:keys`  we found in the interceptor chain.
  (m/decode [:map [:customer-id [:uuid {:decode/json (partial str "Hello-")}]]]
            {:customer-id "facf2030-2dd0-4155-b22b-6a49f7a38e9a"}
            (mt/json-transformer))

  (m/decode [:map [:customer-id [:uuid {:decode {:json (partial str "Hello-")}}]]]
            {:customer-id "facf2030-2dd0-4155-b22b-6a49f7a38e9a"}
            (mt/json-transformer))


  )
