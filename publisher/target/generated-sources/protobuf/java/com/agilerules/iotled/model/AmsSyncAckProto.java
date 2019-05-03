// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: AmsSyncAck.proto

package com.agilerules.iotled.model;

public final class AmsSyncAckProto {
  private AmsSyncAckProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface AmsSyncAckOrBuilder extends
      // @@protoc_insertion_point(interface_extends:proto.AmsSyncAck)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional int64 clientId = 1;</code>
     */
    long getClientId();

    /**
     * <code>optional bool received = 2;</code>
     */
    boolean getReceived();
  }
  /**
   * Protobuf type {@code proto.AmsSyncAck}
   */
  public  static final class AmsSyncAck extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:proto.AmsSyncAck)
      AmsSyncAckOrBuilder {
    // Use AmsSyncAck.newBuilder() to construct.
    private AmsSyncAck(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private AmsSyncAck() {
      clientId_ = 0L;
      received_ = false;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private AmsSyncAck(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 8: {

              clientId_ = input.readInt64();
              break;
            }
            case 16: {

              received_ = input.readBool();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.agilerules.iotled.model.AmsSyncAckProto.internal_static_proto_AmsSyncAck_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.agilerules.iotled.model.AmsSyncAckProto.internal_static_proto_AmsSyncAck_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.class, com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.Builder.class);
    }

    public static final int CLIENTID_FIELD_NUMBER = 1;
    private long clientId_;
    /**
     * <code>optional int64 clientId = 1;</code>
     */
    public long getClientId() {
      return clientId_;
    }

    public static final int RECEIVED_FIELD_NUMBER = 2;
    private boolean received_;
    /**
     * <code>optional bool received = 2;</code>
     */
    public boolean getReceived() {
      return received_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (clientId_ != 0L) {
        output.writeInt64(1, clientId_);
      }
      if (received_ != false) {
        output.writeBool(2, received_);
      }
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (clientId_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, clientId_);
      }
      if (received_ != false) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(2, received_);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck)) {
        return super.equals(obj);
      }
      com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck other = (com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck) obj;

      boolean result = true;
      result = result && (getClientId()
          == other.getClientId());
      result = result && (getReceived()
          == other.getReceived());
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptorForType().hashCode();
      hash = (37 * hash) + CLIENTID_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getClientId());
      hash = (37 * hash) + RECEIVED_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getReceived());
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code proto.AmsSyncAck}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:proto.AmsSyncAck)
        com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAckOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.agilerules.iotled.model.AmsSyncAckProto.internal_static_proto_AmsSyncAck_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.agilerules.iotled.model.AmsSyncAckProto.internal_static_proto_AmsSyncAck_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.class, com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.Builder.class);
      }

      // Construct using com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        clientId_ = 0L;

        received_ = false;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.agilerules.iotled.model.AmsSyncAckProto.internal_static_proto_AmsSyncAck_descriptor;
      }

      public com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck getDefaultInstanceForType() {
        return com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.getDefaultInstance();
      }

      public com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck build() {
        com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck buildPartial() {
        com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck result = new com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck(this);
        result.clientId_ = clientId_;
        result.received_ = received_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck) {
          return mergeFrom((com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck other) {
        if (other == com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck.getDefaultInstance()) return this;
        if (other.getClientId() != 0L) {
          setClientId(other.getClientId());
        }
        if (other.getReceived() != false) {
          setReceived(other.getReceived());
        }
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private long clientId_ ;
      /**
       * <code>optional int64 clientId = 1;</code>
       */
      public long getClientId() {
        return clientId_;
      }
      /**
       * <code>optional int64 clientId = 1;</code>
       */
      public Builder setClientId(long value) {
        
        clientId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 clientId = 1;</code>
       */
      public Builder clearClientId() {
        
        clientId_ = 0L;
        onChanged();
        return this;
      }

      private boolean received_ ;
      /**
       * <code>optional bool received = 2;</code>
       */
      public boolean getReceived() {
        return received_;
      }
      /**
       * <code>optional bool received = 2;</code>
       */
      public Builder setReceived(boolean value) {
        
        received_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional bool received = 2;</code>
       */
      public Builder clearReceived() {
        
        received_ = false;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:proto.AmsSyncAck)
    }

    // @@protoc_insertion_point(class_scope:proto.AmsSyncAck)
    private static final com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck();
    }

    public static com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<AmsSyncAck>
        PARSER = new com.google.protobuf.AbstractParser<AmsSyncAck>() {
      public AmsSyncAck parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new AmsSyncAck(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<AmsSyncAck> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<AmsSyncAck> getParserForType() {
      return PARSER;
    }

    public com.agilerules.iotled.model.AmsSyncAckProto.AmsSyncAck getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_AmsSyncAck_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_AmsSyncAck_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020AmsSyncAck.proto\022\005proto\"0\n\nAmsSyncAck\022" +
      "\020\n\010clientId\030\001 \001(\003\022\020\n\010received\030\002 \001(\010B.\n\033c" +
      "om.agilerules.iotled.modelB\017AmsSyncAckPr" +
      "otob\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_proto_AmsSyncAck_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_proto_AmsSyncAck_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_AmsSyncAck_descriptor,
        new java.lang.String[] { "ClientId", "Received", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
