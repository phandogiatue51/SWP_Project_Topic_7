import React, { useEffect, useState } from "react";
import { useGetAllOrder } from "../../../hooks/order/useGetAllOrder";
import {
  Table,
  Button,
  Tag,
  Modal,
  List,
  Typography,
  Divider,
  Space,
  message,
  Spin,
} from "antd";
import { usePostSendOrder } from "../../../hooks/order/usePostSendOrder";
import { useDeleteOrder } from "../../../hooks/order/useDeleteOrder";

const { Title, Text } = Typography;

const ManageOrder = () => {
  const { data: lstOrder, refetch, isFetching } = useGetAllOrder();
  const [isLoading, setIsLoading] = useState(false);

  const mutation = usePostSendOrder();
  //send userId and orderId
  const { mutate: cancelOrder } = useDeleteOrder();

  useEffect(() => {
    refetch();
  }, []);

  const handleSendClick = (orderId) => {
    mutation.mutate({orderId}, {
      onSuccess: () => {
        message.success("Sent Order !");
        refetch();
      },
      onError: (error) => {
        message.error("Error: " + error.message);
      },
    });
  };

  const handleCancelOrder = (record) => {
    Modal.confirm({
      title: "Cancel Order",
      content: "Are you sure you want to cancel this order?",
      okText: "Yes",
      okType: "danger",
      cancelText: "No",
      onOk() {
        setIsLoading(true);
        cancelOrder(
          { userId: record.userId, orderId: record.id },
          {
            onSuccess: () => {
              toast.success("Order cancelled successfully!");
              refetch();
              setIsLoading(false);
            },
            onError: (error) => {
              toast.error("Failed to cancel order: " + error.message);
            },
          }
        );
      },
    });
  };


  const handleViewClick = (orderId) => {
    let order = lstOrder.find((obj) => {
      return obj.id === orderId;
    });
    Modal.info({
      title: "Order Details",
      content: (
        <div style={{ maxHeight: "60vh", overflow: "auto" }}>
          <Title level={4}>Order #{order.id}</Title>
          <Divider />
          <Text strong>Customer: </Text>
          <Text>{order.fullName}</Text>
          <br />
          <Text strong>Total Amount: </Text>
          <Text>
            {new Intl.NumberFormat("vi-VN", {
              style: "currency",
              currency: "VND",
            }).format(order.totalAmount)}
          </Text>
          <Divider orientation="left">Items</Divider>
          <List
            itemLayout="horizontal"
            dataSource={order.items}
            renderItem={(item, index) => {
              const { name, imageUrl } = item.productId;
              return (
                <List.Item>
                  <List.Item.Meta
                    title={name}
                    description={
                      <div>
                        <Tag color="blue" className="!bg-blue-500 !text-white">
                          Quantity:{" "}
                          <Text className="!text-white">{item.quantity}</Text>
                        </Tag>
                        <Tag color="blue" className="!bg-blue-500 !text-white">
                          Price:{" "}
                          <Text className="!text-white">
                            {new Intl.NumberFormat("vi-VN", {
                              style: "currency",
                              currency: "VND",
                            }).format(item.unitPrice)}
                          </Text>
                        </Tag>
                      </div>
                    }
                    avatar={
                      <img
                        src={imageUrl}
                        alt={name}
                        style={{ width: "50px", height: "50px" }}
                      />
                    }
                    key={index}
                  />
                  <div>
                    <Text strong>
                      {new Intl.NumberFormat("vi-VN", {
                        style: "currency",
                        currency: "VND",
                      }).format(item.quantity * item.unitPrice)}
                    </Text>
                  </div>
                </List.Item>
              );
            }}
          />
        </div>
      ),
    });
  };

  const columns = [
    {
      title: "Order ID",
      dataIndex: "id",
      align: "center",
      width: "8%",
    },
    {
      title: "Full Name",
      dataIndex: "fullName",
      align: "center",
      width: "15%",
      render: (text) => <div style={{ textAlign: 'left' }}>{text}</div>,
    },
    {
      title: "Address",
      dataIndex: "address",
      align: "center",
    },
    {
      title: "Total Amount",
      dataIndex: "totalAmount",
      align: "center",
      width: "15%",
      render: (totalAmount) =>
        `${new Intl.NumberFormat("vi-VN", {
          style: "currency",
          currency: "VND",
        }).format(totalAmount)}`,
    },
    {
      title: "Date order",
      dataIndex: "createdAt",
      width: "10%",
      align: "center",
      render: (createdAt) => {
        return new Date(createdAt).toLocaleString();
      },
    },
    {
      title: "Status",
      dataIndex: "status",
      align: "center",
      width: "10%",
      render: (_, record) => {
        if (record.status === "PENDING") {
          return (
            <Tag
              color="gray"
              className="text-[13px] font-bold w-[150px] !bg-gray-500 !text-white text-center justify-center items-center"
            >
              PENDING
            </Tag>
          );
        }
        if (record.status === "CANCELLED") {
          return (
            <Tag
              color="red"
              className="text-[13px] font-bold w-[150px] !bg-red-500 !text-white text-center justify-center items-center"
            >
              CANCELLED
            </Tag>
          );
        }
        if (record.status === "SUCCESS_PAYMENT") {
          return (
            <Tag
              color="green"
              className="text-[13px] font-bold w-[150px] !bg-green-500 !text-white text-center justify-center items-center"
            >
              WAIT FOR SHIPPING
            </Tag>
          );
        }
        if (record.status === "SHIPPING") {
          return (
            <Tag
              color="blue"
              className="text-[13px] font-bold w-[150px] !bg-blue-500 !text-white text-center justify-center items-center"
            >
              ON DELIVERY
            </Tag>
          );
        }
        if (record.status === "DELIVERED") {
          return (
            <Tag
              color="purple"
              className="text-[13px] font-bold w-[150px] !bg-purple-500 !text-white text-center justify-center items-center"
            >
              DELIVERED
            </Tag>
          );
        }
        //COMPLETED
        if (record.status === "COMPLETED") {
          return (
            <Tag
              color="orange"
              className="text-[13px] font-bold w-[150px] !bg-orange-500 !text-white text-center justify-center items-center"
            >
              COMPLETED
            </Tag>
          );
        }
      },
    },
    {
      title: "Action",
      key: "action",
      align: "center",
      width: "5%",
      render: (_, record) => {
        const buttonStyle = {
          width: "80px",
          height: "32px",
          display: "inline-flex",
          justifyContent: "center",
          alignItems: "center",
          padding: "4px 15px",
          fontSize: "13px",
          borderRadius: "2px",
          textAlign: "center",
        };

        // if (record.status === "SUCCESS_PAYMENT") {
        //   return (
        //     <Button
        //       style={{
        //         ...buttonStyle,
        //         backgroundColor: "#1890ff",
        //         borderColor: "#1890ff",
        //         color: "white",
        //       }}
        //       loading={mutation.isPending}
        //       onClick={() => handleSendClick(record?.id)}
        //     >
        //       Send order
        //     </Button>
        //   );
        // }

        if (
          record.status === "SHIPPING" ||
          record.status === "DELIVERED" ||
          record.status === "COMPLETED"
        ) {
          return (
            <Button
              style={buttonStyle}
              onClick={() => handleViewClick(record?.id)}
            >
              View
            </Button>
          );
        }

        if (record.status === "PENDING") {
          return (
            <Button
              style={{
                ...buttonStyle,
                backgroundColor: "#ff4d4f",
                borderColor: "#ff4d4f",
                color: "white",
              }}
              loading={isLoading}
              onClick={() => handleCancelOrder(record)}
            >
              Cancel
            </Button>
          );
        }

        return null;
      },
    },
  ];

  const data = lstOrder;

  const onChange = (pagination, filters, sorter, extra) => {
    // console.log("params", pagination, filters, sorter, extra);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <Button
        className="mb-[15px] bg-blue-600 text-white hover:!bg-blue-500 hover:!text-white transition-all duration-300 ease-in-out"
        onClick={() => refetch()}
      >
        Refresh Data
      </Button>
      <Table
        loading={isFetching}
        columns={columns}
        dataSource={data}
        onChange={onChange}
        rowKey={(record) => record.id} // Chỉ định giá trị id làm khóa cho mỗi hàng
      />
    </div>
  );
};

export default ManageOrder;
